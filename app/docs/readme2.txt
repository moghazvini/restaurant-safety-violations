Note:
This requirement from user stories:
"As the user moves, I want the dot on the screen to update to a new location, and have the map follow the user so
 the display stays relevant as the user moves through the city."
Forces the map to follow the user and means that the user cannot use the map to see the restaurant pins or
move the map around since the map is forced ontop of the user.

To work around this we made our map focus onto user location every 5 seconds, but clicking on
any pins will disable this focusing on user current location so user can properly use the app to view the pins.
To re activate the map tracing of user location the user can click on the "my location" button on the top right
of the screen.