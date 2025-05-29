import { onSchedule } from "firebase-functions/v2/scheduler";
import { getDatabase } from "firebase-admin/database";
import { initializeApp } from "firebase-admin/app";
import { getMessaging } from "firebase-admin/messaging";

initializeApp();

export const notifyAuctionEnd = onSchedule(
  {
    schedule: "every 5 minutes",
    timeZone: "Europe/Paris",
  },
  async (event) => {
    const db = getDatabase();
    const now = new Date();

    console.log(`[notifyAuctionEnd] Début exécution à ${now.toISOString()}`);

    const offersSnap = await db.ref("offers").once("value");
    const offers = offersSnap.val();

    if (!offers) {
      console.log("[notifyAuctionEnd] Aucun offer trouvé.");
      return;
    }

    for (const offerId in offers) {
      const offer = offers[offerId];
      console.log(`[notifyAuctionEnd] Traitement offerId=${offerId}`, offer);

      // Récupère les enchères pour cette offre
      const bidsSnap = await db.ref("bids").child(offerId).once("value");
      const bids = bidsSnap.val();

      if (!bids || Object.keys(bids).length === 0) {
        console.log(`[notifyAuctionEnd] Aucune enchère pour offer ${offerId}, aucune notification envoyée.`);
        continue;
      }

      // Optionnel : vérifie si la date de fin est passée
      // const offerEndDate = new Date(offer.endDate);
      // if (offerEndDate > now) {
      //   console.log(`[notifyAuctionEnd] L'enchère ${offerId} n'est pas encore terminée.`);
      //   continue;
      // }

      const userId = offer.userId;
      const userSnap = await db.ref("users").child(userId).once("value");
      const user = userSnap.val();
      if (!user || !user.fcmToken) {
        console.log(`[notifyAuctionEnd] Utilisateur ${userId} introuvable ou sans fcmToken.`);
        continue;
      }

      const message = {
        notification: {
          title: "Enchère terminée !",
          body: `Votre enchère "${offer.title}" est terminée. Consultez le résultat dans l'application.`,
        },
        token: user.fcmToken,
      };

      try {
        const response = await getMessaging().send(message);
        console.log(`[notifyAuctionEnd] Notification envoyée à ${userId} pour offer ${offerId}`, response);
      } catch (e) {
        console.error(`[notifyAuctionEnd] Erreur envoi notification FCM à ${userId} pour offer ${offerId}:`, e);
      }
    }

    console.log(`[notifyAuctionEnd] Terminé.`);
  }
);