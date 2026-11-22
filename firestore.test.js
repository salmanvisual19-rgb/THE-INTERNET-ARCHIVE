const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_archivist";
const BOB_UID = "bob_visitor";
const ADMIN_UID = "salman_curator";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// 1. User Favorites Tests
test("Favorites: unauthenticated denied, owner CRUD succeeds, cross-user denied", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("favorites").get()
  );

  // Valid Kotlin-mirrored favorite payload (no `id` field)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("favorites").doc("legend_01").set({
      userId: ALICE_UID,
      itemId: "legend_01",
      itemType: "legend",
      createdAt: now,
    })
  );

  // Ghost field rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("favorites").doc("legend_02").set({
      userId: ALICE_UID,
      itemId: "legend_02",
      itemType: "legend",
      extraField: true,
      createdAt: now,
    })
  );

  // Cross-user read & write rejected
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("favorites").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("favorites").doc("legend_01").delete()
  );

  // Owner read & delete succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("favorites").get()
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("favorites").doc("legend_01").delete()
  );
});

// 2. Trend Battles & Atomic Voting Tests
test("Trend Battles: atomic batch vote succeeds once, prevents double voting & raw tampering", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const past = new Date(Date.now() - 5000);

  // Seed battle with 0 votes
  await assertSucceeds(
    aliceDb.collection("battles").doc("nyan_vs_doge").set({
      title: "Icons of Wholesome Chaos",
      contenderA: "Nyan Cat",
      contenderB: "Doge",
      eraTag: "2010s",
      status: "active",
      votesA: 0,
      votesB: 0,
      createdAt: past,
      updatedAt: past,
    })
  );

  // Aligned list query succeeds; unfiltered query fails
  await assertSucceeds(
    aliceDb.collection("battles").where("status", "==", "active").get()
  );
  await assertFails(aliceDb.collection("battles").get());

  // Direct vote count tampering without vote doc fails
  await assertFails(
    aliceDb.collection("battles").doc("nyan_vs_doge").update({
      votesA: 500,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Atomic WriteBatch: create vote doc + increment matching contender
  const batch1 = aliceDb.batch();
  const battleRef = aliceDb.collection("battles").doc("nyan_vs_doge");
  const voteRef = battleRef.collection("votes").doc(ALICE_UID);
  const voteTime = new Date(Date.now() - 1000);
  batch1.set(voteRef, {
    userId: ALICE_UID,
    battleId: "nyan_vs_doge",
    choice: "A",
    createdAt: voteTime,
  });
  batch1.update(battleRef, {
    votesA: 1,
    updatedAt: voteTime,
  });
  await assertSucceeds(batch1.commit());

  // Second vote by same user fails
  const batch2 = aliceDb.batch();
  batch2.set(voteRef, {
    userId: ALICE_UID,
    battleId: "nyan_vs_doge",
    choice: "B",
    createdAt: new Date(Date.now() - 500),
  });
  batch2.update(battleRef, {
    votesB: 1,
    updatedAt: new Date(Date.now() - 500),
  });
  await assertFails(batch2.commit());
});

// 3. Community Submissions & Curator Admin Tests
test("Submissions: user submits pending, cannot self-approve; verified admin moderates; unverified email spoof rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const spoofedAdminDb = testEnv.authenticatedContext("spoof_uid", {
    email: "salmankheri9@gmail.com",
    email_verified: false,
  }).firestore();
  const verifiedAdminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "salmankheri9@gmail.com",
    email_verified: true,
  }).firestore();

  const created = new Date(Date.now() - 5000);

  // Alice cannot submit directly as approved
  await assertFails(
    aliceDb.collection("submissions").doc("sub_bad").set({
      userId: ALICE_UID,
      authorName: "Alice",
      title: "Fake Approved",
      eraYear: 2012,
      category: "Meme",
      story: "Trying to bypass moderation queue.",
      status: "approved",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Alice submits pending artifact (optional quote omitted/present)
  await assertSucceeds(
    aliceDb.collection("submissions").doc("sub_01").set({
      userId: ALICE_UID,
      authorName: "Alice",
      title: "Keyboard Cat",
      eraYear: 2007,
      category: "Legend",
      story: "Fatso the cat playing an upbeat tune on a keyboard in 1984, went viral in 2007.",
      quote: "Play him off, Keyboard Cat!",
      status: "pending",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Bob cannot list pending or unfiltered submissions
  await assertFails(
    bobDb.collection("submissions").where("status", "==", "pending").get()
  );
  await assertFails(bobDb.collection("submissions").get());

  // Alice can list her own submissions
  await assertSucceeds(
    aliceDb.collection("submissions").where("userId", "==", ALICE_UID).get()
  );

  // Alice cannot self-approve
  await assertFails(
    aliceDb.collection("submissions").doc("sub_01").update({
      status: "approved",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Unverified email spoofing admin fails
  await assertFails(
    spoofedAdminDb.collection("submissions").doc("sub_01").update({
      status: "approved",
      curatorNote: "Spoofed",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Verified admin lists pending and approves
  await assertSucceeds(
    verifiedAdminDb.collection("submissions").where("status", "==", "pending").get()
  );
  await assertSucceeds(
    verifiedAdminDb.collection("submissions").doc("sub_01").update({
      status: "approved",
      curatorNote: "Verified historical internet artifact.",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Now Bob can query approved submissions
  await assertSucceeds(
    bobDb.collection("submissions").where("status", "==", "approved").get()
  );
});
