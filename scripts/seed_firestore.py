"""
Seed Firestore with sample menu, dress code, and timeline data.

Usage:
    python3 scripts/seed_firestore.py --key path/to/service-account.json
    python3 scripts/seed_firestore.py --key path/to/service-account.json --wedding <weddingId>
"""

import argparse
import sys
import warnings
warnings.filterwarnings("ignore")

import firebase_admin
from firebase_admin import credentials, firestore

# ── Sample data (mirrors old hardcoded values) ─────────────────────────────────

SAMPLE_MENU = [
    {
        "courseName": "Starter",
        "emoji": "🥗",
        "items": ["Bruschetta", "Caesar Salad", "Tomato Bisque", "Shrimp Cocktail"],
    },
    {
        "courseName": "Main Course",
        "emoji": "🍽",
        "items": ["Chicken Marsala", "Pan-Seared Salmon", "Vegan Risotto", "Beef Tenderloin"],
    },
    {
        "courseName": "Dessert",
        "emoji": "🍰",
        "items": ["Wedding Cake", "French Macarons", "Crème Brûlée"],
    },
    {
        "courseName": "Drinks",
        "emoji": "🥂",
        "items": ["Champagne Toast", "House Wine", "Signature Cocktail", "Mocktails"],
    },
]

SAMPLE_DRESS_CODE = {
    "style": "Semi-Formal",
    "colorHexes":  ["#FDFAF5", "#F5E6C8", "#D4848A", "#8C7B6E"],
    "colorLabels": ["Ivory",   "Champagne", "Dusty Rose", "Taupe"],
    "suggested": ["Cocktail dress", "Tailored suit", "Elegant midi dress"],
    "avoid":     ["Jeans", "White dress", "Sportswear"],
}

SAMPLE_TIMELINE = [
    {
        "time": "14:00", "title": "Guest Arrival",
        "description": "The venue opens its doors. Welcome to our special day — we are so glad you are here.",
        "iconName": "groups", "status": "completed",
    },
    {
        "time": "14:30", "title": "Welcome Drinks",
        "description": "Enjoy champagne and canapés on the garden terrace while the venue fills with love.",
        "iconName": "wine_bar", "status": "completed",
    },
    {
        "time": "15:00", "title": "Ceremony Begins",
        "description": "The wedding ceremony starts in the garden chapel. Please take your seats.",
        "iconName": "favorite", "status": "current",
    },
    {
        "time": "16:00", "title": "Cocktail Hour",
        "description": "Celebrate with cocktails and light music while we capture memories.",
        "iconName": "local_bar", "status": "upcoming",
    },
    {
        "time": "18:00", "title": "Dinner Service",
        "description": "A three-course dinner awaits in the grand ballroom. Please find your seat.",
        "iconName": "restaurant", "status": "upcoming",
    },
    {
        "time": "19:30", "title": "First Dance",
        "description": "Join us as we share our first dance as husband and wife.",
        "iconName": "music_note", "status": "upcoming",
    },
    {
        "time": "20:30", "title": "Cake Cutting",
        "description": "Gather around as we cut the wedding cake and raise a toast to the future.",
        "iconName": "cake", "status": "upcoming",
    },
    {
        "time": "21:30", "title": "After Party",
        "description": "Dance the night away with live music, entertainment, and open bar.",
        "iconName": "celebration", "status": "upcoming",
    },
    {
        "time": "23:00", "title": "Farewell",
        "description": "As our magical evening draws to a close — thank you for celebrating with us.",
        "iconName": "nights_stay", "status": "upcoming",
    },
]

# ──────────────────────────────────────────────────────────────────────────────

def seed(db, wedding_id=None):
    payload = {
        "menu":      SAMPLE_MENU,
        "dressCode": SAMPLE_DRESS_CODE,
        "timeline":  SAMPLE_TIMELINE,
    }

    weddings_ref = db.collection("weddings")

    if wedding_id:
        doc_ref = weddings_ref.document(wedding_id)
        if not doc_ref.get().exists:
            print(f"ERROR: Wedding '{wedding_id}' not found.")
            sys.exit(1)
        doc_ref.update(payload)
        print(f"✓ Updated wedding: {wedding_id}")
    else:
        docs = list(weddings_ref.stream())
        if not docs:
            print("No wedding documents found in 'weddings' collection.")
            sys.exit(1)
        for doc in docs:
            doc.reference.update(payload)
            print(f"✓ Updated wedding: {doc.id}")

    print("\nDone! Reload the app to see the changes.")


def main():
    parser = argparse.ArgumentParser(description="Seed WedNow Firestore data")
    parser.add_argument("--key", required=True, help="Path to Firebase service account JSON")
    parser.add_argument("--wedding", default=None, help="Specific wedding document ID (optional; updates all if omitted)")
    args = parser.parse_args()

    cred = credentials.Certificate(args.key)
    firebase_admin.initialize_app(cred, {"projectId": "wednow-5192d"})
    db = firestore.client()

    seed(db, args.wedding)


if __name__ == "__main__":
    main()
