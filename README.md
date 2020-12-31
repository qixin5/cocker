# Cocker

Cocker is a code search technique. It takes as input a piece of code and finds code that is syntactically similar to it in a code database.

If you use Cocker, please cite our paper:
```
@inproceedings{xin2017leveraging,
  title={Leveraging syntax-related code for automated program repair},
  author={Xin, Qi and Reiss, Steven P.},
  booktitle={Proceedings of the 32nd IEEE/ACM International Conference on Automated Software Engineering (ASE)},
  pages={660--670},
  year={2017},
  organization={IEEE}
}
```

## Installation and usage

0. Prerequisite: Linux. JDK 1.8. Ant. PostgreSQL.

If you haven't installed PostgreSQL, refer to https://www.postgresql.org/download.


1. Make sure you have a role (user) named postgres and you have a password for it.

To achieve this, I tried the commands below on my machine.
```
sudo -u postgres psql postgres
\password postgres
Control+D
```


2. Update PostgreSQL's property file.

In Database.props (in cocker), replace dbmspassword with your own password.


3. Set COCKER_HOME.

Add `export COCKER_HOME=XXX/cocker` (XXX is the parent directory of cocker) in the end of ~/.profile. Then run `source ~/.profile`.


4. Set up Ivy.

```
cd lib
java -cp ivy.jar edu.brown.cs.ivy.exec.IvySetup -local
cd ..
```

After this, you should have access to a file named ~/.ivy/Props. The content of this file should be something like:

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
<comment>SETUP on Mon Dec 28 01:20:57 UTC 2020</comment>
<entry key="edu.brown.cs.ivy.IVY">/home/qxin6/cocker/lib</entry>
<entry key="BROWN_IVY_IVY">/home/qxin6/cocker/lib</entry>
</properties>
```

NOTE: You may see error message including " java.net.UnknownHostException: valerie". This is fine.


5. Use your own index path.

```
1. Make sure you have an empty directory named XXX/vol/cocker to save index files.
2. Modify analysis/src/AnalysisConstants.java by replacing "/home/qxin6" with XXX (your own path) for the definition of INDEX_PATH_PREFIX.
```

Note: Use ABSOLUTE path.


6. Build cocker.

`ant compile`


7. Create the database.

```
cd bin
./cockerdb -a SSFIX new
```


8. Index code files.

```
cd bin
./cockercmd -a SSFIX start -m <CODE_DIR_PATH> -M 16000
```

* NOTE: Replace <CODE_DIR_PATH> with ABSOLUTE path to the directory holding code files (e.g., Merobase) to be indexed.
* NOTE: Make sure you use a large memory (e.g., 16G as above).
* NOTE: Depending on how many files you want to index, this may take a while. After indexing, you should see index files in XXX/vol/cocker/cocker-index-ssfix


9. Stop and restart server before code search.

```
1. cd bin
2. ./cockercmd -a SSFIX -stop
3. Wait for a few seconds (e.g., 15s)
4. ./cockercmd -a SSFIX -start -M 16000
5. Wait for a few seconds (e.g., 15s) before you do any code search
```


10. Code search.

```
cd bin
./cockercmd -a SSFIX -data <QUERY_LOC> <QUERY_FILE>
```

* <QUERY_FILE> is the .java file that conains the query code.
* <QUERY_LOC> is a string used to fetch query statements in the file. The format of this string is "slc:X,Y", where X is the line number and Y is the column number. If you have multiple query statements, create a loc string for each, and then use a string that concatenates these strings by semicolon(s). For example, if you want to use two statements that start at lines #4 and #5 (colums #1) as query, provide a loc string "slc:4,1;slc:5,1".


11. Quick test.

```
cd bin
./cockerq_mytest
```

If successful, you should be able to see output like the following.

```
file:///home/qxin6/mycodedatabase/testgen/FlexTestGen.java,slc:83,5,0.25485426
file:///home/qxin6/mycodedatabase/testgen/MakeTestGen.java,slc:227,10,0.22162989
file:///home/qxin6/mycodedatabase/testgen/SedTestGen.java,slc:110,10,0.21562001
file:///home/qxin6/mycodedatabase/testgen/Schedule2TestGen.java,slc:57,5;slc:58,5,0.2129162
file:///home/qxin6/mycodedatabase/testgen/FlexTestGen.java,slc:90,2,0.2129162
file:///home/qxin6/mycodedatabase/testgen/ScheduleTestGen.java,slc:57,5;slc:58,5,0.2129162
file:///home/qxin6/mycodedatabase/testgen/SedTestGen.java,slc:95,3,0.2129162
file:///home/qxin6/mycodedatabase/testgen/SedTestGen.java,slc:120,2,0.2129162
file:///home/qxin6/mycodedatabase/testgen/MakeTestGen.java,slc:233,2,0.2129162
file:///home/qxin6/mycodedatabase/testgen/GzipTestGen.java,slc:96,5,0.2129162
```



### Index new files

When you want to index new files in directory XXX, do the following.

```
1. cd bin
2. ./cockerq -a SSFIX -m XXX -M 16000 (suppose the server is running, otherwise add " -start" in the end)
3. ./cockerq -a SSFIX -stop
4. Wait for a few seconds (e.g., 15s)
5. ./cockerq -a SSFIX -start -M 16000
```


### Final notes

Before you do code search, make sure you've started the server.


