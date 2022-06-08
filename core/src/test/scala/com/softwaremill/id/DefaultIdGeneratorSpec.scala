package com.softwaremill.id

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DefaultIdGeneratorSpec extends AnyFlatSpec with Matchers {
  val workerMask = 0x000000000001f000L
  val datacenterMask = 0x00000000003e0000L
  val timestampMask = 0xffffffffffc00000L

  class EasyTimeWorker(workerId: Long, datacenterId: Long, timeStart: Long = System.currentTimeMillis())
      extends IdWorker(workerId, datacenterId) {
    var timeMaker = () => timeStart
    override def timeGen(): Long = {
      timeMaker()
    }
  }

  class WakingIdWorker(workerId: Long, datacenterId: Long) extends EasyTimeWorker(workerId, datacenterId) {
    var slept = 0
    override def tilNextMillis(lastTimestamp: Long): Long = {
      slept += 1
      super.tilNextMillis(lastTimestamp)
    }
  }

  behavior of "IdWorker"

  it should "generate an id" in {
    val s = new IdWorker(1, 1)
    val id: Long = s.nextId()
    id should be > 0L
  }

  it should "return an accurate timestamp" in {
    val s = new IdWorker(1, 1)
    val t = System.currentTimeMillis
    (s.get_timestamp() - t) should be < 50L
  }

  it should "return the correct job id" in {
    val s = new IdWorker(1, 1)
    s.get_worker_id() should be(1L)
  }

  it should "return the correct dc id" in {
    val s = new IdWorker(1, 1)
    s.get_datacenter_id() should be(1L)
  }

  it should "properly mask worker id" in {
    val workerId = 0x1f
    val datacenterId = 0
    val worker = new IdWorker(workerId, datacenterId)
    for (i <- 1 to 1000) {
      val id = worker.nextId()
      ((id & workerMask) >> 12) should be(workerId)
    }
  }

  it should "properly mask dc id" in {
    val workerId = 0
    val datacenterId = 0x1f
    val worker = new IdWorker(workerId, datacenterId)
    val id = worker.nextId()
    ((id & datacenterMask) >> 17) should be(datacenterId)
  }

  it should "properly mask timestamp" in {
    val worker = new EasyTimeWorker(31, 31)
    for (i <- 1 to 100) {
      val t = System.currentTimeMillis
      worker.timeMaker = () => t
      val id = worker.nextId()
      ((id & timestampMask) >> 22) should be(t - worker.epoch)
    }
  }

  it should "roll over sequence id" in {
    // put a zero in the low bit so we can detect overflow from the sequence
    val workerId = 4
    val datacenterId = 4
    val worker = new IdWorker(workerId, datacenterId)
    val startSequence = 0xffffff - 20
    val endSequence = 0xffffff + 20
    worker.sequence = startSequence

    for (i <- startSequence to endSequence) {
      val id = worker.nextId()
      ((id & workerMask) >> 12) should be(workerId)
    }
  }

  it should "generate increasing ids" in {
    val worker = new IdWorker(1, 1)
    var lastId = 0L
    for (i <- 1 to 100) {
      val id = worker.nextId()
      id should be > lastId
      lastId = id
    }
  }

  it should "generate 1 million ids quickly" in {
    val worker = new IdWorker(31, 31)
    val t = System.currentTimeMillis
    for (i <- 1 to 1000000) {
      var id = worker.nextId()
      id
    }
    val t2 = System.currentTimeMillis
    println("generated 1000000 ids in %d ms, or %,.0f ids/second".format(t2 - t, 1000000000.0 / (t2 - t)))
    1 should be > 0
  }

  it should "sleep if we would rollover twice in the same millisecond" in {
    var queue = new scala.collection.mutable.Queue[Long]()
    val worker = new WakingIdWorker(1, 1)
    val iter = List(2L, 2L, 3L).iterator
    worker.timeMaker = () => iter.next
    worker.sequence = 4095
    worker.nextId()
    worker.sequence = 4095
    worker.nextId()
    worker.slept should be(1)
  }

  it should "generate only unique ids" in {
    val worker = new IdWorker(31, 31)
    var set = new scala.collection.mutable.HashSet[Long]()
    val n = 2000000
    (1 to n).foreach { i =>
      val id = worker.nextId()
      if (set.contains(id)) {
        println(java.lang.Long.toString(id, 2))
      } else {
        set += id
      }
    }
    set.size should be(n)
  }

  it should "generate ids over 50 billion" in {
    val worker = new IdWorker(0, 0)
    worker.nextId() should be > 50000000000L
  }

  it should "generate ids older then lower bound" in {
    // given
    val worker = new IdWorker(0, 0)
    val lowerBound = worker.idForTimestamp(System.currentTimeMillis())

    // when
    val ids = List(worker.nextId(), worker.nextId(), worker.nextId())

    // then
    ids.foreach(_ >= lowerBound should be(true))
  }

  it should "generate older lowerBound then next generated ids from distinct workers" in {
    // given
    val worker = new IdWorker(0, 0)
    val lowerBound = worker.idForTimestamp(System.currentTimeMillis())

    // when
    val ids = List(
      new DefaultIdGenerator(workerId = 1).nextId(),
      new DefaultIdGenerator(workerId = 2).nextId(),
      new DefaultIdGenerator(workerId = 3).nextId()
    )

    // then
    ids.foreach(_ >= lowerBound should be(true))
  }

  it should "generate range of ids that catches only 3 oldest ids" in {
    // given
    val currentPoint = System.currentTimeMillis()
    val upperBoundOverheadAndExtraTime = 300000 // (datacenterId << datacenterIdShift) | (workerId << workerIdShift)

    val gen = new EasyTimeWorker(0, 1, timeStart = currentPoint)
    val lowerBound = gen.idForTimestamp(currentPoint)
    val upperBound = lowerBound + upperBoundOverheadAndExtraTime

    val ids = List(gen.nextId(), gen.nextId(), gen.nextId())

    // when
    val laterMilis = 120
    val olderIds = new EasyTimeWorker(1, 2, timeStart = currentPoint + laterMilis)
    val newList = ids ++ List(olderIds.nextId(), olderIds.nextId(), olderIds.nextId())

    // then
    newList.count(id => id >= lowerBound && id < upperBound) should be(3)
  }
}
