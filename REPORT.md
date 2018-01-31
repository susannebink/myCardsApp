## Short Discription
With this app, you will be able to store all your customer and privilage cards in one place. Just scan the barcode of your card and you can
easily access it via your phone. Find the nearest store for all your cards and add them to your favorites.

## Technical Design
The user will first start at Login Activity, in this activity the user can (obviously) sign in to their account to go to Overview Activity
or go to Register Activity to register. At Register activity, the user can create an account which will be verified through Firebase 
Authentication. Once a user is registered, the user will be send to Overview Activity.
Overview Activity gives an overview of the user's cards. The user can select a card by tapping on the card name in the listview, the card 
will then be shown in ShowCard Activity. The user can delete a card by long clicking the card name in the listview.
