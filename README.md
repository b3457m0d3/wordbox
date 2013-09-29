TODO

Ordering of favourited words: options for chronological, alphabetical (drop down on up button thing).
Able to remove favourited words during the quiz. (i.e. mark as learned)
Quiz ordering modes, not just random order.
Clear favourites option in the settings.
Remove translations from definitions.
New icon.
Create my own dictionary in a SQL database. There is a Searchable Dictionary sample that I could use as reference http://developer.android.com/tools/samples/index.html.
If you shuffle in quiz mode and hit the back button you get all your different orderings. Not sure what would be better, but that isn't very helpful.


BUGS

Can get stranded in the empty MainActivity by hitting the back button.
If you are in the list of favourited words, click a word, unfavourite it, and hit back, you get the old list of favourites (the one you just unfavourited is still there).


NOTES

I don't think the contentprovider for the dictionary is set up properly to handle the built in search functionality suggested results. The format of the URI is different than what the system expects. If I want to do suggestions I have to either write my own UI or my own content provider.

