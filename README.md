# SearchEngine
This project is based on java and spring boot to complete a search engine that allows users to get relevant web pages by entering keywords. 
The first stage of the project is to carry out the collection of data. 
This phase uses multi-threading to improve collection efficiency and uses etymology, stopword, mapping and other methods to save storage space. 
The second phase of the project is a web search, consisting of a single keyword search and a two keyword search. 
The final web page information is displayed by spring boot.

# Quickly Start
Download the code and run 'SearchEngineApplication'  
If it is the first time you run the code, it need some time to load the data.  
The data will be saved in data_table.ser  
After data are collected, you can go to localhost:8080  
You can type one single word or two word with certain format(word1+word2,word1-word2,word1!word2)  

# Some Details  
Data Structures 
  
(1).DataTable  
The gathered data is stored in DataTable. It has an index whose data type is Map<String, Set<PageInfo>>. In other words, the index is a map from a keyword, to a set of PageInfo objects.  
By using the index, we can perform O(1) time search for a set of page metadata, given a keyword. It is also storage-efficient, as it only stores references to PageInfo objects, rather than multiple duplicated data.  
  
(2).PageInfo  
PageInfo is an object to store the information of a website including the title and link. All keywords related to this website will point to this object, which will avoid duplicate storage of the same website and save storage space.
  
(3).URL  
A collection of URLs to be processed. When information about a website is extracted, the links contained in this website are filtered and stored in the URL Pool for processing. The URL Pool can store at most 10 URLs and the capacity of the URL Pool is controlled by the variable U. 
  
(4).PURL  
A collection of processed URLs. Once a site's information has been extracted, the site will be stored in the PURL Pool. The PURL Pool can store at most 100 URLs and the capacity of the URL Pool is controlled by the variable V.  

