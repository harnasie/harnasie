const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendNotification = functions.https.onRequest((req, res) => {
  const message = {
    notification: {
      title: "Zagrożenie",
      body: "Nadchodzące niebezpieczeństwo w twojej okolicy!",
    },
    topic: "dangers",
  };

  admin.messaging().send(message)
      .then((response) => {
        res.status(200).send("Powiadomienie wysłane: " + response);
      })
      .catch((error) => {
        res.status(500).send("Błąd przy wysyłaniu powiadomienia: " + error);
      });
});


