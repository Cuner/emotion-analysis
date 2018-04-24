# emotion-analysis
analysis emotionality of sentences

- Step One : get word list from db or xml
```$xslt
// get data from db
List<Word> wordList = wordRepository.getWordListFromDB();
// get data from xml
List<Word> wordList = wordRepository.getWordListFromXML();
```

- Step Two : load and init dictionary
```$xslt
//load
Dictionary.getInstance().loadDictionary(wordList);
//init
Dictionary.getInstance().initDistionary();
```

- Step Three : define spliting word library
```$xslt
for (Word word : wordList) {
    UserDefineLibrary.insertWord(word.getName(), "userDefine", 1000);
}
```

- Step Four : analysis emotion of sentences
```$xslt
Parser parser = new Parser();
List<Emotion> emotionList = parser.parseElement("一次性买了几件，质量很好没话说，，太值了，朋友都说很个性。");
```