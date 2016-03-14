// Databricks notebook source exported at Mon, 14 Mar 2016 03:47:26 UTC
// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)

// COMMAND ----------

// MAGIC %md
// MAGIC #Introduction to Spark SQL
// MAGIC * This notebook explains the motivation behind Spark SQL
// MAGIC * It introduces interactive SparkSQL queries and visualizations
// MAGIC * This notebook uses content from Databricks SparkSQL notebook and [SparkSQL programming guide](http://spark.apache.org/docs/latest/sql-programming-guide.html)

// COMMAND ----------

// MAGIC %md
// MAGIC Some resources on SQL
// MAGIC * [https://en.wikipedia.org/wiki/SQL](https://en.wikipedia.org/wiki/SQL)
// MAGIC * [https://en.wikipedia.org/wiki/Apache_Hive](https://en.wikipedia.org/wiki/Apache_Hive)
// MAGIC * [http://www.infoq.com/articles/apache-spark-sql](http://www.infoq.com/articles/apache-spark-sql)
// MAGIC * [https://databricks.com/blog/2015/02/17/introducing-dataframes-in-spark-for-large-scale-data-science.html](https://databricks.com/blog/2015/02/17/introducing-dataframes-in-spark-for-large-scale-data-science.html)
// MAGIC * **READ**: [https://people.csail.mit.edu/matei/papers/2015/sigmod_spark_sql.pdf](https://people.csail.mit.edu/matei/papers/2015/sigmod_spark_sql.pdf)
// MAGIC 
// MAGIC Some of them are embedded below in-place for your convenience.

// COMMAND ----------

// MAGIC %run "/scalable-data-science/xtraResources/support/sdsFunctions"

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/SQL",500))

// COMMAND ----------

displayHTML(frameIt("https://en.wikipedia.org/wiki/Apache_Hive#HiveQL",175))

// COMMAND ----------

displayHTML(frameIt("https://databricks.com/blog/2015/02/17/introducing-dataframes-in-spark-for-large-scale-data-science.html",600))

// COMMAND ----------

// MAGIC %md
// MAGIC This is an elaboration of the [Apache Spark 1.6 sql-progamming-guide](http://spark.apache.org/docs/latest/sql-programming-guide.html).
// MAGIC 
// MAGIC # Getting Started
// MAGIC 
// MAGIC ## Starting Point: SQLContext
// MAGIC 
// MAGIC The entry point into all functionality in Spark SQL is 
// MAGIC * the [`SQLContext`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.SQLContext) class, 
// MAGIC * or one of its descendants, e.g. [`HiveContext`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.hive.HiveContext). 
// MAGIC 
// MAGIC To create a basic `SQLContext`, all you need is a `SparkContext`. 
// MAGIC 
// MAGIC Conveniently, in Databricks notebook (similar to `spark-shell`) `SQLContext` is already created for you and is available as `sqlContext`.

// COMMAND ----------

// Cntrl+Enter will print sqlContext available in notebook
sqlContext

// COMMAND ----------

// MAGIC %md 
// MAGIC **Deeper Dive:** (beginners skip for now)
// MAGIC 
// MAGIC Usually, when building application (running on production-like on-premises cluster) you would manually create `SQLContext` using something like this:
// MAGIC ```scala
// MAGIC // An existing SparkContext
// MAGIC val sc: SparkContext = ...
// MAGIC val sqlContext = new org.apache.spark.sql.SQLContext(sc)
// MAGIC 
// MAGIC // This is used to implicitly convert an RDD to a DataFrame (see examples below)
// MAGIC import sqlContext.implicits._
// MAGIC ```
// MAGIC 
// MAGIC Note that SQLContext in notebook is actually HiveContext. The difference is that HiveContext provides richer functionality over standard SQLContext, e.g. window functions were only available with HiveContext up to Spark 1.5.2, or usage of Hive user-defined functions. This originates from the fact that Spark SQL parser was built based on HiveQL parser, so only HiveContext was supporting full HiveQL syntax. Now it is changing, and SQLContext supports most of the functionality of the descendant (window functions should be available in SQLContext in 1.6+).
// MAGIC 
// MAGIC > Note that you do not need Hive installation when working with SQLContext or HiveContext, Spark comes with built-in derby datastore.

// COMMAND ----------

// MAGIC %md
// MAGIC ## Creating DataFrames
// MAGIC 
// MAGIC With a [`SQLContext`](http://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.SQLContext), applications can create [`DataFrame`](https://spark.apache.org/docs/latest/api/scala/index.html#org.apache.spark.sql.DataFrame) 
// MAGIC * from an existing `RDD`, 
// MAGIC * from a Hive table, or 
// MAGIC * from various other data sources.
// MAGIC 
// MAGIC #### Just to recap: 
// MAGIC * A DataFrame is a distributed collection of data organized into named columns. 
// MAGIC * You can think of it as being organized into table RDD of case class `Row` (which is not exactly true). 
// MAGIC * DataFrames, in comparison to RDDs, are backed by rich optimizations, including:
// MAGIC   * tracking their own schema, 
// MAGIC   * adaptive query execution, 
// MAGIC   * code generation including whole stage codegen, 
// MAGIC   * extensible Catalyst optimizer, and 
// MAGIC   * project [Tungsten](https://databricks.com/blog/2015/04/28/project-tungsten-bringing-spark-closer-to-bare-metal.html). 
// MAGIC 
// MAGIC > Note that performance for DataFrames is the same across languages Scala, Java, Python, and R. This is due to the fact that the only planning phase is language-specific (logical + physical SQL plan), not the actual execution of the SQL plan.
// MAGIC 
// MAGIC ![DF speed across languages](https://databricks.com/wp-content/uploads/2015/02/Screen-Shot-2015-02-16-at-9.46.39-AM-1024x457.png)

// COMMAND ----------

// MAGIC %md
// MAGIC ## DataFrame Basics
// MAGIC 
// MAGIC #### 1. An empty DataFrame
// MAGIC #### 2. DataFrame from a range
// MAGIC #### 3. DataFrame from an RDD
// MAGIC #### 4. DataFrame from a table

// COMMAND ----------

// MAGIC %md
// MAGIC #### 1. Making an empty DataFrame
// MAGIC Spark has some of the pre-built methods to create simple DataFrames
// MAGIC * let us make an Empty DataFrame

// COMMAND ----------

val emptyDF = sqlContext.emptyDataFrame // Ctrl+Enter to make an empty DataFrame

// COMMAND ----------

// MAGIC %md
// MAGIC Not really interesting, or is it?
// MAGIC 
// MAGIC **You Try!**
// MAGIC 
// MAGIC Put your cursor after `emptyDF.` below and hit Tab to see what can be done with `emptyDF`.

// COMMAND ----------

emptyDF.

// COMMAND ----------

// MAGIC %md
// MAGIC #### 2. Making a DataFrame from a range
// MAGIC Let us make a DataFrame next
// MAGIC * from a range of numbers, as follows:

// COMMAND ----------

val rangeDF = sqlContext.range(0, 3) // Ctrl+Enter to make DataFrame with 0,1,2

// COMMAND ----------

// MAGIC %md
// MAGIC Note that Spark automatically names column as `id` and casts integers to type `bigint` for big integer or Long.
// MAGIC 
// MAGIC In order to get a preview of data in DataFrame use `show()` as follows:

// COMMAND ----------

rangeDF.show() // Ctrl+Enter

// COMMAND ----------

// MAGIC %md
// MAGIC #### 3. Making a DataFrame from an RDD
// MAGIC 
// MAGIC * Make an RDD
// MAGIC * Conver the RDD into a DataFrame using the defualt `.toDF()` method
// MAGIC * Conver the RDD into a DataFrame using the non-default `.toDF(...)` method
// MAGIC * Do it all in one line

// COMMAND ----------

// MAGIC %md
// MAGIC Let's first make an RDD using the `sc.parallelize` method, transform it by a `map` and perform the `collect` action to display it, as follows:

// COMMAND ----------

val rdd1 = sc.parallelize(1 to 5).map(i => (i, i*2))
rdd1.collect() // Ctrl+Enter

// COMMAND ----------

// MAGIC %md
// MAGIC Next, let us convert the RDD into DataFrame using the `.toDF()` method, as follows:

// COMMAND ----------

val df1 = rdd1.toDF() // Ctrl+Enter 

// COMMAND ----------

// MAGIC %md
// MAGIC As it is clear, the DataFrame has columns named `_1` and `_2`, each of type `int`.  Let us see its content using the `.show()` method next.

// COMMAND ----------

df1.show() // Ctrl+Enter

// COMMAND ----------

// MAGIC %md
// MAGIC Note that by default, i.e. without specifying any options as in `toDF()`, the column names are given by `_1` and `_2`.
// MAGIC 
// MAGIC We can easily specify column names as follows:

// COMMAND ----------

val df1 = rdd1.toDF("single", "double") // Ctrl+Enter
df1.show()

// COMMAND ----------

// MAGIC %md
// MAGIC Of course, we can do all of the above steps to make the DataFrame `df1` in one line and then show it, as follows:

// COMMAND ----------

val df1 = sc.parallelize(1 to 5).map(i => (i, i*2)).toDF("single", "double") //Ctrl+enter
df1.show()

// COMMAND ----------

// MAGIC %md
// MAGIC **You Try!**
// MAGIC 
// MAGIC Fill in the `???` below to get a DataFrame `df2` whose first two columns are the same as `df1` and whose third column named triple has values that are three times the values in the first column.

// COMMAND ----------

val df2 = sc.parallelize(1 to 5).map(i => (i, i*2, ???)).toDF("single", "double", "triple") // Ctrl+enter after editing ???
df2.show()

// COMMAND ----------

// MAGIC %md
// MAGIC #### 4. Making a DataFrame from a table
// MAGIC We can load existing tables as DataFrames.  We will later see how to create tables from RDDs or other sources of raw data, including csv files, etc.
// MAGIC 
// MAGIC First let's see what tables are available to us.

// COMMAND ----------

sqlContext.tables.show() // Ctrl+Enter to see available tables

// COMMAND ----------

// MAGIC %md
// MAGIC Let us load the table with `tableName` `diamonds` as a DataFrame (assuming it exixts!, if not don't worry as diamonds is our next stop!), as follows:

// COMMAND ----------

val diamondsDF = sqlContext.table("diamonds") // Shift+Enter

// COMMAND ----------

display(diamondsDF)

// COMMAND ----------

// MAGIC %md
// MAGIC ### 5. Using SQL for interactively querying a table is very powerful!
// MAGIC Note `-- comments` are how you add `comments` in SQL cells beginning with `%sql`.
// MAGIC 
// MAGIC * You can run SQL `select *` statement to see all columns of the table, as follows:
// MAGIC   * This is equivalent to the above `display(diamondsDF)' with the DataFrame

// COMMAND ----------

// MAGIC %sql
// MAGIC -- Ctrl+Enter to select all columns of the table
// MAGIC select * from diamonds

// COMMAND ----------

// MAGIC %md
// MAGIC ***
// MAGIC ***
// MAGIC 
// MAGIC ## Next we will play with data
// MAGIC 
// MAGIC The data here is **semi-structured tabular data** (Tab-delimited text file in dbfs).
// MAGIC Let us see what Anthony Joseph in BerkeleyX/CS100.1x had to say about such data.
// MAGIC 
// MAGIC ### Key Data Management Concepts: Semi-Structured Tabular Data
// MAGIC 
// MAGIC **(watch now 1:26)**:
// MAGIC 
// MAGIC [![Semi-Structured Tabular Data by Anthony Joseph in BerkeleyX/CS100.1x](http://img.youtube.com/vi/G_67yUxdDbU/0.jpg)](https://www.youtube.com/v/G_67yUxdDbU?rel=0&autoplay=1&modestbranding=1&start=1)
// MAGIC 
// MAGIC ***

// COMMAND ----------

// MAGIC %md
// MAGIC Those who want to understand SparkSQL functionalities in more detail can see the [video lectures in Module 3 of Anthony Joseph's Introduction to Big data edX course](/#workspace/scalable-data-science/xtraResources/edXBigDataSeries2015/CS100-1x/Module 3: Lectures) from the Community Edition of databricks.  The course has been added to this databricks shard at [/#workspace/scalable-data-science/xtraResources/edXBigDataSeries2015/CS100-1x](/#workspace/scalable-data-science/xtraResources/edXBigDataSeries2015/CS100-1x) as extra resources for the project-focussed course [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/).

// COMMAND ----------

// MAGIC %md
// MAGIC 
// MAGIC # [Scalable Data Science](http://www.math.canterbury.ac.nz/~r.sainudiin/courses/ScalableDataScience/)
// MAGIC 
// MAGIC 
// MAGIC ### prepared by [Raazesh Sainudiin](https://nz.linkedin.com/in/raazesh-sainudiin-45955845) and [Sivanand Sivaram](https://www.linkedin.com/in/sivanand)
// MAGIC 
// MAGIC *supported by* [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/databricks_logoTM_200px.png)](https://databricks.com/)
// MAGIC and 
// MAGIC [![](https://raw.githubusercontent.com/raazesh-sainudiin/scalable-data-science/master/images/AWS_logoTM_200px.png)](https://www.awseducate.com/microsite/CommunitiesEngageHome)