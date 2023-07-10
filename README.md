# Collect data on Vietnam's history in Wikipedia and DBPedia.

This is a mini-project in Object Oriented Programming course of SoICT - HUST.

- [Problem](#problem)
- [Abstract](#abstract)
- [Folder structure](#folder-structure)
- [Packages description](#packages-description)
- [Result](#result)
- [Contributors to this project](#contributors-to-this-project)
- [VS Code for Java](#vs-code-for-java)

Problem
-------
There are numerous websites (such as [Người kể sử](https://nguoikesu.com), [`Wikipedia`](https://vi.wikipedia.org/wiki), [`DBPedia`](https://www.dbpedia.org/), etc.) that offer information on Vietnamese history. Finding these sites, automatically gathering data on Vietnamese history, and connecting this data are all necessary.

Entities to collect include: 
- **The historical dynasties of Vietnam** (Prehistoric period, Hong Bang, An Duong Vuong, the first Northern Domination,...)

- **Vietnamese historical figures** (For example, Vietnamese kings can be found in [Wikipedia:Vua_Việt_Nam](https://vi.wikipedia.org/wiki/Vua_Vi%E1%BB%87t_Nam))

- **Tourist sites** (temples, communal houses, shrines, mounds, etc.) and **Vietnamese historical sites**
- **Vietnamese cultural festivals**
- **Historical events in Vietnam**

Each entity needs `identifiers`, `attributes`, and importantly, entities that need to be `associated` with each other. Some examples:

Hung Temple Festival:
- Venue: Held in Viet Tri City, Phu Tho province
- Organization date: 10/3 lunar calendar
- Related historical figures: Remembering Hung King
- Related events: King Hung built the country
- Related relics: Hung Temple
- …

Historical figures of Hung King:
- Father: Lac Long Quan
- Ascension to the throne: 2524 BC
- Year of birth: unknown
- Year of death: unknown
- …

Abstract
--------

### Wikipedia
Entities web pages on Wikipedia often have a [Wikidata](https://www.wikidata.org/wiki/Wikidata:Main_Page) item (from the Wikipedia website: Tools -> Wikidata Item). When accessing, we will see a lot of properties of the entity in the form of tables. This is **Structured Data.**

*"Each Item or Property has a persistent URI made up of the Wikidata concept namespace and the Item or Property ID (e.g., Q42, P31) as well as concrete data that can be accessed by that Item's or Property's data URL."*

See this: [How to get JSON from a Item or Property](https://www.wikidata.org/wiki/Wikidata:Data_access#Linked_Data_Interface_(URI))

Hence, for **Wikipedia** , we can get properties of an entity as long as we have its Wikidata ID.

I use the [`Wikidata query builder`](https://query.wikidata.org/querybuilder/?uselang=en) to query all entities that have property "P17" ("country") of "Q881"("Vietnam"). After that, for each entity we crawl from Wikidata, I will check if it is related to Vietnam or not.

Finally, our job now is to transform all of that Wikipedia's .json files into **human-readable** format.

### DBPedia

DBPedia also provide **Structured Data.** Morover, compared to Wikipedia, the data in the dbpedia JSON file is **simpler**, but it also contains **a lot of inaccurate information.**
Entities with **links to entities in Wikipedia** are the only ones I scratch and process. For each entity, I also only receive properties **whose components share the same data type**.

Folder structure
----------------
```
.
├── data                 		    # consists of fully analyzed information.
│   └── địa điểm du lịch, di tích lịch sử
│   │   └── ...
│   └── lễ hội văn hóa
│   │   └── ...
│   └── nhân vật lịch sử
│   │   └── Q36014.json                         # json file of entity
│   │   └── ...
│   └── sự kiện lịch sử
│   │   └── Q482456.json
│   │   └── ...
│   └── triều đại lịch sử
│   │   └── ...
├── raw                 	            # data collected for processing
│   └── Wikipedia                                
│   │   └── data                                # final data of all scratched entities in human-readable form
│   │   └── intitialize                         # folder containing all input data
│   │   └── logs                                # folder containing all data in all processing steps
│   └── DBPedia
│   │   └── ...                        
├── src                 	            # source code
│   ├── crawler
│   │   └── datamanage                 		# the program's general data processing and management
│   │   │   └── ...
│   │   └── wikidatacrawler                     # the crawler for Wikipedia
│   │   │   └── ...
│   │   └── dbpediadatacrawler                 	# the crawler for DBPedia
│   │   │   └── ...
│   │   └── crawldata.java                      # this file is used to run the data scraper program.
│   │   └── myinterface                           # gives a summary of the important classes and what they are for.
│   │   │   └── ...								
│   ├── text-modify                         # this folder contains files for modifying data.
│   │   │   └── change_name.json                # change name of some properties
│   │   │   └── delete.txt                      # delete invalid entities
│   │   │   └── ...
│   ├── Statistical.java                    # used for statistics on scratched data.
├── statistic.json                 	    # statistics on the number of entities collected
```

Packages description
--------------------

### datamanage: general data processing and management

- `DataHandling.java`: contains all the static methods for handling different data types (JSON, read-write file, string decode, ...etc)

- `DataFolder.java`: initialize shared folders used in scraping data for different sites (input for crawling, final data folder, all crafted URLs, logs folders for crawling and later analyzed,...)

- `BruteForceData.java`: An abstract class for the brute-force algorithm. Idea: start with one (or several) preselected URLs, take all the URLs in it, analyze them to select quality URLs, and repeat with those quality URLs.

- `Merge.java`: Concatenate data together. For conflicting fields, separate them and source each field. For identical fields, combine them and concatenate sources to assign them to that field.

- `ModifyData.java`: Change, edit, or delete the given attributes if they are meaningless or duplicate in meaning. It also deletes the entity and unlinks it with other entities if the entity is not related to Vietnamese history.

### myinterface: gives a summary of the important classes.

- `WikiBruteForceCrawler.java`: Ihe interface represents the workflow of the brute force method for wiki crawler. First of all, we will get all entities related to Vietnam. Secondly, check if the entity being analyzed contains one of the Vietnam-related entities in its properties. Next, write all important information to the files, such as converting from URL to entity ID, getting the names of the properties of the entities, and saving the associated entities. Finally, write all attributes and associated entities to files in JSON format.

- `WikiSelectiveCrawler.java`: Ihe interface represents the workflow of the selective method for wiki crawler. We will query all of them, anlyzed them and then assign them some important properties for identification and classification.

- `WikiTableCrawler.java`: Ihe interface represents the workflow of the table method for wiki crawler. We only need query all of them and then assign them some important properties for identification and classification.

- `WikiCrawler.java`: This class implements the `WikiBruteForceCrawler`, `WikiSelectiveCrawler`, and `WikiTableCrawler` interfaces to crawl data from Wiki. It handles the data retrieval process by calling the respective methods from each interface. Furthermore, it includes functionality for assigning associated entities and exporting the final data. 

- `NonWikiCrawler.java`: Interface for non-wiki crawler (DBPedia, Nguoi ke su,...) include getData method for crawling raw data and syncData method for matching with Wiki data.

### wikidatacrawler: the crawler for Wikipedia

- `WikiFolder.java`: Inherit from the `DataFolder` class, initialize directories solely for the analysis of data from Wikipedia.

- `WikiDataHandling.java`: contains all the static methods for handling different properties and entities.

- `WikiBruteForceData.java`: Inherit from the `BruteForceData` class, implement from `WikiBruteForceCrawler` interface, and override the **"analyzeEntity"** method. The idea is to take entities related to Vietnamese history, save them and then analyze the URLs in those entities to assign them to associative entities.

- `WikiSelectiveData.java`: Inherit **"analyzeEntity"** method from the `BruteForceData` class and implement from `WikiSelectiveCrawler` interface. The idea is to takes all entities in some specific URLs and assign them to be related to Vietnamese history without any checking.

- `WikiTableData.java`: Implement from `WikiTableCrawler` interface. The idea is that we will takes data from the tables provided in Wikipedia on a specific number of pages and assigns it to entities. If any of the entities in the table have an entity ID that represents them, then match the properties in the table to the properties contained in the data with that entity ID. If not, initialize a separate entity ID with properties that are in the table. Entity IDs are initialized with an X at the end of their ID.

- `WikiDataExport.java`: Export the final data to data folder.

- `WikiData.java`: Call all the classes above to make a complete Wikipedia crawler.

### dbpediadatacrawler: the crawler for DBPedia

- `DBPediaData.java`: Inherit from the `BruteForceData` class, implement from `NonWikiCrawler` interface, and override the **"analyzeEntity"** method. The idea is to take entities related to Vietnamese history, save them and then analyze the URLs in those entities to assign them to associative entities. Finally, sync the data to match with the format of Wiki data.

Result
------
 
I processed and retrieved **8735** entities related to Vietnamese history out of a total of **more than 400,000** entities that have been scratched.

Contributors to this project
----------------------------

Sincere thanks to:

- [Nguyễn Quốc Trung](https://github.com/ravenpwn) for contributing data by scraping the tables in Wikipedia for processing in the WikiTableData method. 
- [Chử Minh Hà](https://github.com/Nov17th) for filtering and reporting a lot of non-Vietnamese entities that cannot be processed by computers.

VS Code for Java
------------

Here is a guideline to help you get started to write Java code in Visual Studio Code.

### Folder Structure for Java in VS Code

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

### Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).
