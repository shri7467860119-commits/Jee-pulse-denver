const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

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

function serverTimestamp() {
  return new Date(Date.now() - 1000);
}

function validAttemptPayload(userId) {
  return {
    userId,
    testTitle: "JEE Main 2025 Physics Drill",
    testType: "PYQ_PRACTICE",
    subjectFilter: "Physics",
    totalQuestions: 10,
    attemptedCount: 8,
    correctCount: 6,
    incorrectCount: 2,
    scoreObtained: 22,
    maxScore: 40,
    accuracyPercent: 75,
    timeSpentSeconds: 600,
    xpEarned: 150,
    weakTopics: ["Rotational Motion"],
    strongTopics: ["Electrostatics"],
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };
}

function validLeaderboardPayload(userId) {
  return {
    userId,
    displayName: "Aarav Sharma",
    xpPoints: 450,
    currentStreak: 3,
    bestStreak: 5,
    questionsSolved: 25,
    correctAnswers: 20,
    testsCompleted: 3,
    highScorePercent: 85,
    badgesUnlocked: ["first_step", "streak_3"],
    targetExam: "JEE Main & Adv 2026",
    visibility: "public",
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };
}

function validStudyTaskPayload(userId) {
  return {
    userId,
    dayLabel: "Day 1 - Today",
    subject: "Physics",
    topic: "Rotational Motion",
    activityType: "PYQ_DRILL",
    targetPyqCount: 15,
    durationMinutes: 45,
    priorityLevel: "HIGH_WEAK_AREA",
    completed: false,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  };
}

// 1. Unauthenticated Rejections
test("Unauthenticated user cannot read or write test_attempts or leaderboard", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("test_attempts").get());
  await assertFails(unauthDb.collection("leaderboard").get());
  await assertFails(
    unauthDb.collection("test_attempts").doc("att_1").set(validAttemptPayload(ALICE_UID))
  );
});

// 2. Authenticated Owner CRUD on test_attempts
test("Authenticated user can create and query their own test_attempts", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("test_attempts").doc("att_alice_1").set(validAttemptPayload(ALICE_UID))
  );
  await assertSucceeds(
    aliceDb.collection("test_attempts").where("userId", "==", ALICE_UID).get()
  );
});

// 3. Cross-User Isolation on test_attempts
test("Authenticated user cannot read or modify another user's test_attempts", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("test_attempts").doc("att_alice_1").set(validAttemptPayload(ALICE_UID))
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("test_attempts").doc("att_alice_1").get());
  await assertFails(bobDb.collection("test_attempts").where("userId", "==", ALICE_UID).get());
  await assertFails(bobDb.collection("test_attempts").doc("att_alice_1").delete());
});

// 4. Client Query Alignment (Rules are not filters)
test("Authenticated user fails test_attempts query without userId filter", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("test_attempts").get());
});

// 5. Leaderboard Ownership & Public Visibility Query Alignment
test("Authenticated user can upsert own leaderboard profile and query public leaderboard", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("leaderboard").doc(ALICE_UID).set(validLeaderboardPayload(ALICE_UID))
  );
  // Cannot spoof another user's leaderboard doc
  await assertFails(
    aliceDb.collection("leaderboard").doc(BOB_UID).set(validLeaderboardPayload(BOB_UID))
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb.collection("leaderboard").where("visibility", "==", "public").get()
  );
});

// 6. Study Task CRUD and Shadow Update Rejection
test("Authenticated user can create/update own study_tasks, but shadow update fails", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("study_tasks").doc("task_1").set(validStudyTaskPayload(ALICE_UID))
  );
  await assertSucceeds(
    aliceDb.collection("study_tasks").doc("task_1").update({
      completed: true,
      updatedAt: serverTimestamp(),
    })
  );
  // Shadow update with ghost field must fail
  await assertFails(
    aliceDb.collection("study_tasks").doc("task_1").update({
      completed: true,
      isAdmin: true,
      updatedAt: serverTimestamp(),
    })
  );
});
