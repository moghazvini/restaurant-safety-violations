Note:
We are using a sqLite database, which makes the update time last about 30-60s, or more
depending on the device hardware. After the data has finished downloading, the updating
dialog will not be able to cancel, even though the cancel button still exists. If the cancel
button does not respond, it means the download is finished, but the insertions are still
running. Only press Cancel in the first 3s, otherwise it may cause the app to crash for
unresponsiveness.

App language is dependent on current system language and will change upon device language change.
Data, such as restaurant name, long violation description, and addresses are not translated.