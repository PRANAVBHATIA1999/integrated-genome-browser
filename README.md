# Integrated Genome Browser

## About 

The Integrated Genome Browser (IGB, pronounced ig-bee) is a fast, free, and highly interactive desktop genome browser ideal for exploring and understanding large-scale data sets from genomics. Originally developed at Affymetrix, IGB is now open source software. 

IGB is also an extensible visual analytics platform. Developers can use IGB to create new visual analysis functions called IGB Apps.

To get IGB, clone this repository or download an [IGB installer for your platform].

Visit:

* [BioViz Web site](https://bioviz.org) for platform-specific installers
* Searchable on-line [User's Guide](https://wiki.bioviz.org/confluence/display/igbman/Home)
* IGB programming [on-line class](https://canvas.instructure.com/courses/1164217)
* [IGB Channel on YouTube](https://www.youtube.com/c/IntegratedGenomeBrowser) with video tutorials and demos
* [IGB Jira Issue Tracker site](http://jira.bioviz.org) describes development plans (requires login to see most content)
* Article describing [Integrated Genome Browser: Visual analytics platform for genomics](http://bioinformatics.oxfordjournals.org/content/early/2016/04/04/bioinformatics.btw069.long) 

***

# To build and run IGB 

1. Install JDK 21 equipped with Java FX. (Older IGB branches require JDK 8 or earlier.)
2. Install Apache mvn, required to build IGB the way the IGB development team does it. 
3. Clone the [team repository](https://bitbucket.org/lorainelab/integrated-genome-browser) to your desktop. The default branch is the main development branch.
4. Build IGB using maven. Skip tests to save time.
5. Run IGB using the run_igb script for your platform.

### Example

```
git clone https://bitbucket.org/lorainelab/integrated-genome-browser
cd integrated-genome-browser
mvn clean install -DskipTests=true
./run_igb.sh
```

# To build IGB using Bitbucket.org pipelines

You can also build IGB using Bitbucket pipelines and a Docker image with the same JDK we use to distribute IGB. 

To do build a jar (java archive) file for IGB using Bitbucket pipelines: 

1. Create an account at Bitbucket.org.
2. Fork this repository in your account.
3. Enable bitbucket pipelines in your forked repository. 
4. Configure an App password following instructions in bitbucket-pipelines.yml.
5. Visit the Branches section of your repository and right-click the branch you want to build. 
6. Choose "Run pipeline for branch" and select "manual-build." 


If the pipeline fails, bitbucket will let you know. If the copying step worked, you should see `igb_exe.jar` listed in your repository's Downloads section.




***

# Developing IGB Apps

IGB runs in an OSGi container, which supports adding and removing pluggable Apps while IGB is running.
For details and advice on how to write IGB Apps, see:

* [Developing IGB Apps](https://wiki.bioviz.org/confluence/display/igbdevelopers/Developing+IGB+Apps) in the IGB Developer's Guide
* [OSGi tutorials](https://blog.stackleader.com/tags/osgi/) by IGB core developer alumni at [Stackleader.com](https://stackleader.com)
* [IGB App Store](https://apps.bioviz.org/) where you can upload and distribute Apps to IGB users

***

# How to contribute

IGB is an open source project, and the core development group welcomes your contributions. If you would like to contribute a new feature or improvements of any kind, please do!

To contribute to the IGB code base, please use a fork-and-branch workflow:

1. Fork the [team repository](https://www.bitbucket.org/lorainelab/integrated-genome-browser).
2. Create branches specific to the changes you want to make, push to your fork.
3. Issue pull requests to the team repository's default branch from the branch on your fork.

***

# Other info

* Once IGB has started, you can view the status of its component OSGi bundles by visiting the [Felix Web console](http://localhost:7080/system/console/bundles) on your local computer. You can use the web console to reload bundles after rebuilding them.  
**Note: ** If you are accessing Felix Web Console for the first time then after accessing the link, login using Username: admin, Password: admin
* IGB uses the [Genoviz SDK](https://bitbucket.org/lorainelab/genoviz-sdk), an open source Java Swing-based library of "widgets" designed for genomic data visualization that was first developed at UC Berkeley. 

*** 

# Questions? 

Visit the [Bioviz help page](http://bioviz.org/help.html)
