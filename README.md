# Id generator

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/id-generator_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.common/id-generator_2.12)

Generate unique ids. A default generator is provided, based on [Twitter Snowflake](https://github.com/twitter/snowflake),
which generates time-based ids. Besides that library provide `IdPrettifier` which may convert `Long` into user friendly id such `HPJD-72036-HAPK-58077`. `IdPrettifier` preserve Long's monotonicity, provides checksum and produce id with constant length (if it's not configured otherwise). It also maybe configured to user custom part sizes, separator or don't use leasing zeros to provide fixed length. More information you will find in the [blogpost](https://blog.softwaremill.com/new-pretty-id-generator-in-scala-commons-39b0fc6b6210) about it.

SBT depedency:

````scala
libraryDependencies += "com.softwaremill.common" %% "id-generator" % "1.2.1"
````

Examples
```scala
//create instance of it
val generator:StringIdGenerator = PrettyIdGenerator.singleNode

//generate ids
val stringId = generator.nextId()
stringId shouldNot be(empty)
stringId should fullyMatch regex """[A-Z]{4}-[0-9]{5}-[A-Z]{4}-[0-9]{5}"""

//or it might be used just for encoding existing ids
val prettifier = IdPrettifier.default
val id = prettifier.prettify(100L) //id = AAAA-00000-AAAA-01007
id should be("AAAA-00000-AAAA-01007")

//get seed
val origin = prettifier.toIdSeed(id) // 100L
origin should be(Right(100L))

//use custom prettifier
val customPrettifier = IdPrettifier.custom(encoder = new AlphabetCodec(new Alphabet("ABC")), partsSize = 4, delimiter = '_', leadingZeros = false)
val customId = customPrettifier.prettify(1234567L) //BCAACAB_5671

//construct custom PrettyIdGenerator
val idGenerator:IdGenerator = new IdGenerator {
  override def nextId(): Long = ???
  override def idBaseAt(timestamp: Long): Long = ???
}
val customGenerator = new PrettyIdGenerator(idGenerator, customPrettifier)
```   

