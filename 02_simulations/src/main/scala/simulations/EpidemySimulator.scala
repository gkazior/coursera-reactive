package simulations

import math.random

/**
 * Models people in the world of rooms
 */
class EpidemySimulator extends Simulator {

  def randomBelow(i: Int) = (random * i).toInt

  protected[simulations] object SimConfig {
    val population: Int = 300
    val roomRows: Int = 8
    val roomColumns: Int = 8
    val prevalenceRate: Double = 0.01
    //val prevalenceRate: Double = 0.1
    val transmissibilityRate: Double = 0.4
    val deathProbabilityWhenSick: Double = 0.25
    val moveInDays: Int = 5
    val vipToAllRate: Double = 0.00
    val personMustMove: Boolean = false

    val infectionDelay: Int = 1
    val incubationDelay: Int = 6 // becomes sick after being infected after 6 days
    val deathDelay: Int = 14
    val immuneDelay: Int = 16
    val notImmuneDelay: Int = 18
  }
  protected[simulations] object SimSmallConfig {
    val population: Int = 5
    val roomRows: Int = 2
    val roomColumns: Int = 2
    val prevalenceRate: Double = 0.6
    val transmissibilityRate: Double = 0.4
    val deathProbabilityWhenSick: Double = 0.25
    val moveInDays: Int = 5
    val vipToAllRate: Double = 0.05
    val personMustMove: Boolean = false

    val infectionDelay: Int = 1
    val incubationDelay: Int = 6 // becomes sick after being infected after 6 days
    val deathDelay: Int = 14
    val immuneDelay: Int = 16
    val notImmuneDelay: Int = 18
  }

  protected[simulations] object ReduceMobiltyConfig {
    val population: Int = 300
    val roomRows: Int = 8
    val roomColumns: Int = 8
    val prevalenceRate: Double = 0.01
    val transmissibilityRate: Double = SimConfig.transmissibilityRate / 2
    val deathProbabilityWhenSick: Double = 0.25
    val moveInDays: Int = 5
    val vipToAllRate: Double = 0.05
    val personMustMove: Boolean = false

    val incubationDelay: Int = 6 // becomes sick after being infected after 6 days
    val deathDelay: Int = 14
    val immuneDelay: Int = 16
    val notImmuneDelay: Int = 18
  }

  import SimConfig._
  override def reportStats() {
    // println(stats)
  }

  case class CountOkFail(var ok: Int = 0, var fail: Int = 0) {
    def Ok() = { ok += 1 }
    def Fail() = { fail += 1 }
    def rate() = if (ok + fail == 0) 0.0 else ok.toDouble / (ok + fail)
    override def toString: String = {
      f"$rate%2.2f/$ok%2.2f/$fail%2.2f"
    }
  }
  case class CountOkFail2(var ok: Int = 0, var fail: Int = 0, var fail2: Int = 0, var fail1Label: String = "fail1", var fail2Label: String = "fail2") {
    def Ok() = { ok += 1 }
    def Fail() = { fail += 1 }
    def Fail2() = { fail2 += 1 }
    def rate() = if (ok + fail + fail2 == 0) 0.0 else ok.toDouble / (ok + fail + fail2)
    override def toString: String = {
      f"$rate%2.2f/$ok%2.2f/$fail%2.2f/$fail2%2.2f"
    }
  }
  case class SimpleStat(var min: Double = 0.0, var max: Double = 0.0, var sum: Double = 0.0, var count: Int = 0) {
    def update(value: Double): Unit = {
      if (value == 0) return // on inialization
      if (value <= 0.0) throw new IllegalStateException("values passed to update should be positive")
      if (count == 0) {
        min = value
        max = value
      }
      if (value < min) min = value
      if (value > max) max = value
      sum += value
      count += 1
    }
    def update(value: Int): Unit = {
      update(value.toDouble)
    }
    def avg() = sum / count

    override def toString: String = {
      f"$avg%2.2f/$min%2.2f/$max%2.2f"
    }
  }

  object stats {
    val moveRate: CountOkFail = new CountOkFail
    val goSickRate: CountOkFail = new CountOkFail
    val immuneRate: CountOkFail = new CountOkFail
    val notImmuneRate: CountOkFail = new CountOkFail

    val dieRate: CountOkFail2 = new CountOkFail2 { fail2Label = "Not sick" }
    var infectRate: CountOkFail2 = new CountOkFail2 { fail2Label = "No infection source" }

    val moveInterval: SimpleStat = new SimpleStat
    val infectionInterval: SimpleStat = new SimpleStat
    val sickInterval: SimpleStat = new SimpleStat
    val deadAfter: SimpleStat = new SimpleStat
    val sickAfterInfection: SimpleStat = new SimpleStat

    def infectedTimesRate(): Double = infectRate.ok / population

    override def toString: String = {
      s"moveRate[$moveRate] infected[$infectRate] sick[$goSickRate] die[$dieRate] immune[$immuneRate] notImmune[$notImmuneRate]" +
        f" infectedTimes[$infectedTimesRate%2.2f] " +
        s" deadAfter[$deadAfter] move[$moveInterval] infect[$infectionInterval] sick[$sickInterval] sickAfterInfection[$sickAfterInfection]"
    }
  }

  type RoomAddress = (Int, Int) // Row and column

  def mayBeInfected(person: Person): Boolean = {
    // There is sb sick, dead or infected in my room
    persons exists ((other: Person) =>
      (other != person) &&
        ((other.row, other.col) == (person.row, person.col)) &&
        (other.dead || other.sick || other.infected))
  }
  def roomLookNice(add: RoomAddress) = {
    // there is no visibly sick person in the room
    !(persons exists ((person: Person) =>
      ((person.row, person.col) == add) &&
        (person.dead || person.sick)))
  }

  class Person(val id: Int) {
    private var actions: List[Simulator#Action] = List()
    def addAction(a: Simulator#Action) {
      actions = a :: actions
      a()
    }

    override def toString(): String = {
      //s"Person[$row][$col]"
      s"Person[$infected][$sick][$immune][$dead]"
    }
    var lastMovedAt: Int = 0
    var lastSickAt: Int = 0
    var lastDeadAt: Int = 0
    var lastInfectedAt: Int = 0

    var infected = false
    var sick = false
    var immune = false
    var dead = false
    var vip = random < vipToAllRate // 5% of population is Vip. vaccines available from the beginning

    // demonstrates random number generation
    var row: Int = randomBelow(roomRows)
    var col: Int = randomBelow(roomColumns)

    private def nextRoom(add: RoomAddress, personMustMove: Boolean): Option[RoomAddress] = {
      def nextRoomIdx(currIdx: Int, move: Int, lowBound: Int, highBound: Int): Int = {
        if (currIdx + move < lowBound) highBound
        else if (currIdx + move > highBound) lowBound
        else currIdx + move
      }
      def getRandomItem(list: List[(Int, Int)], predicate: ((Int, Int)) => Boolean): Option[(Int, Int)] = {
        if (list.isEmpty) None
        else {
          val randomItem = randomBelow(list.size)
          val randomAddress = list(randomItem)
          if (predicate(randomAddress)) {
            Some(randomAddress)
          } else {
            getRandomItem(list.take(randomItem - 1) ++ list.takeRight(list.size - 1 - randomItem), predicate)
          }
        }
      }
      val neighbourAddresses: List[(Int, Int)] = List((nextRoomIdx(add._1, 1, 0, roomRows - 1), add._2), (nextRoomIdx(add._1, -1, 0, roomRows - 1), add._2), (add._1, nextRoomIdx(add._2, 1, 0, roomColumns - 1)), (add._1, nextRoomIdx(add._2, -1, 0, roomColumns - 1)))

      val predicate = if (personMustMove)
        (pair: (Int, Int)) => true
      else
        (pair: (Int, Int)) => roomLookNice(pair)

      getRandomItem(neighbourAddresses, predicate)
    }

    private def moveTo(): Unit = {
      if (dead) {
        // At the time of inserting an event we may do not know if the person will be dead at the time when moveTo is processed
        return
        //throw new IllegalStateException("Dead person cannot move")
      }

      val roomTo = nextRoom((row, col), personMustMove)

      if (roomTo.nonEmpty) {

        stats.moveInterval.update(getCurrentTime - lastMovedAt)
        lastMovedAt = getCurrentTime

        stats.moveRate.Ok
      } else {
        stats.moveRate.Fail
      }
      // Try to get infected no matter in my or other room
      if (!infected && !dead && !sick && !immune && !vip) {
        if (mayBeInfected(this)) {
          if (random <= transmissibilityRate) {
            doInfect()
          } else {
            stats.infectRate.Fail
          }
        } else {
          stats.infectRate.Fail2
        }
      }
      makeMoveEv()
    }

    def doInfect() {
      if (dead) {
        return
      }
      infected = true

      stats.infectionInterval.update(getCurrentTime - lastInfectedAt)

      lastInfectedAt = getCurrentTime

      makeSickEv()
      makeDeathEv()
      makeImmuneEv()
      makeNotImmuneEv()
      stats.infectRate.Ok
    }
    
    def goSick() {
      if (dead) {
        return
      }

      if (infected) {
        sick = true

        stats.sickInterval.update(getCurrentTime - lastSickAt)
        stats.sickAfterInfection.update(getCurrentTime - lastInfectedAt)
        lastSickAt = getCurrentTime

        stats.goSickRate.Ok
      } else {
        stats.goSickRate.Fail
      }
    }

    def die() {
      if (dead) {
        return
      }

      if (sick) {
        assert(!dead)
        assert(sick)
        assert(infected)

        if (random <= deathProbabilityWhenSick) {
          dead = true
          sick = false
          infected = false

          stats.deadAfter.update(getCurrentTime - lastDeadAt)
          lastDeadAt = getCurrentTime

          stats.dieRate.Ok
        } else {
          stats.dieRate.Fail
        }
      } else {
        stats.dieRate.Fail2
      }
    }
    def makeImmune() {
      if (dead) {
        return
      }
      if (sick) {
        assert(infected)
        assert(!dead)
        assert(!immune)

        sick = false
        immune = true

        stats.immuneRate.Ok
      } else {
        stats.immuneRate.Fail
      }
    }

    def makeNotImmune() {
      if (dead) {
        return
      }
      if (immune) {
        assert(!sick)
        assert(infected)
        assert(!dead)

        immune = false
        infected = false
 
        stats.notImmuneRate.Ok
      } else {
        stats.notImmuneRate.Fail
      }
    }

    //
    // Events
    //
    private def addTheAction(delay: Int, delayedAction: => () => Unit) = {
      def theDelayedAction() {
        afterDelay(delay) { delayedAction() }
      }
      addAction(theDelayedAction)
    }
    // I am adding here the infectionDelay since events are generated on together with infection
    def makeSickEv() = { addTheAction(incubationDelay, goSick) }
    def makeDeathEv() = { addTheAction(deathDelay, die) }
    def makeNotImmuneEv() = { addTheAction(notImmuneDelay, makeNotImmune) }

    def makeMoveEv() {
      def moveAction() {
        if (!dead) {
          val moveDelay = randomBelow(moveInDays) + 1 // move in 1 .. 5
          afterDelay(moveDelay) { moveTo() }
        }
      }
      addAction(moveAction)
    }

    def makeImmuneEv() {
      def immuneEvent() {
        if (!dead)
          afterDelay(immuneDelay) { makeImmune() }
      }
      addAction(immuneEvent)
    }
  }

  val persons: List[Person] = List.range(0, population) map (new Person(_)) // to complete: construct list of persons

  // Setup initial infection
  def setupInfection() {
    // We expect exactly prevalenceRate on the infection init
    // the code 
    // person.infected = random <= prevalenceRate
    // may be inaccurate
    val infectedPersonCnt: Int = Math.round(population * prevalenceRate).toInt

    persons filter (!_.vip) take (infectedPersonCnt) map (_.doInfect())

    val vipNo: Int = persons.filter((_.vip)).size
    //println(s"Have $vipNo of vips")

    persons map (_.makeMoveEv)
  }

  setupInfection()
}

