package nodescala

import scala.concurrent._
import scala.util._

object TryWs {
  sealed case class Coin(name: String)
  val gold = Coin("gold")                         //> gold  : nodescala.TryWs.Coin = Coin(gold)
  val silver = Coin("silver")                     //> silver  : nodescala.TryWs.Coin = Coin(silver)

  sealed case class Treasure(name: String)
  val diamond = Treasure("diamond")               //> diamond  : nodescala.TryWs.Treasure = Treasure(diamond)
  val magicWand = Treasure("wand")                //> magicWand  : nodescala.TryWs.Treasure = Treasure(wand)

  def eatenByMonster(a: Adventure): Boolean = {
    false
  }                                               //> eatenByMonster: (a: nodescala.TryWs.Adventure)Boolean

  sealed case class GameOverException(comment: String) extends RuntimeException

  class Adventure {
    def collectCoins(): List[Coin] = {
      if (eatenByMonster(this))
        throw new GameOverException("Ooops")
      List(gold, gold, silver)
    }
    def buyTreasure(coins: List[Coin]): Treasure = {
      //      if (coins.sumBy(_.value) < treasureCost)
      //        throw new GameOverException("Nice try!")
      diamond
    }

  }
  object Adventure {
    def apply(arg: Int) = { new Adventure() }
  }

  val adventure = Adventure(2)                    //> adventure  : nodescala.TryWs.Adventure = nodescala.TryWs$$anonfun$main$1$Adv
                                                  //| enture$2@1c458c1e

  //val coins = adventure.collectCoins()
  val treasure = for {
    coins <- Try(adventure.collectCoins())
    treasure <- Try(adventure.buyTreasure(coins))
  } yield treasure                                //> treasure  : scala.util.Try[nodescala.TryWs.Treasure] = Success(Treasure(dia
                                                  //| mond))

}