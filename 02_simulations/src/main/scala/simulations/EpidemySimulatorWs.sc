package simulations




object EpidemySimulatorWs extends EpidemySimulator {
  import SimConfig._

  
  persons                                         //> res0: List[simulations.EpidemySimulatorWs.Person] = List(Person[true][false]
                                                  //| [false][false], Person[true][false][false][false], Person[true][false][false
                                                  //| ][false], Person[false][false][false][false], Person[false][false][false][fa
                                                  //| lse], Person[false][false][false][false], Person[false][false][false][false]
                                                  //| , Person[false][false][false][false], Person[false][false][false][false], Pe
                                                  //| rson[false][false][false][false], Person[false][false][false][false], Person
                                                  //| [false][false][false][false], Person[false][false][false][false], Person[fal
                                                  //| se][false][false][false], Person[false][false][false][false], Person[false][
                                                  //| false][false][false], Person[false][false][false][false], Person[false][fals
                                                  //| e][false][false], Person[false][false][false][false], Person[false][false][f
                                                  //| alse][false], Person[false][false][false][false], Person[false][false][false
                                                  //| ][false], Person[false][false][false][false], Person[false][false][false][fa
                                                  //| lse], Person[false][false][false][false], Person[false][false][false][false]
                                                  //| , Person[false][false][false][false], Person[false][false][false][false], Pe
                                                  //| rson[false][false][false][false], Person[false][false][false][false], Person
                                                  //| [false][false][false][false], Person[false][false][false][false], Person[fal
                                                  //| se][false][false][false], Person[false][false][false][false], Person[false][
                                                  //| false][false][false], Person[false][false][false][false], Person[false][fals
                                                  //| e][false][false], Person[false][false][false][false], Person[false][false][f
                                                  //| alse][false], Person[false][false][false][false], Person[false][false][false
                                                  //| ][false], Person[false][false][false][false], Person[false][false][false][fa
                                                  //| lse], Person[false][false][false][false], Person[false][false][false][false]
                                                  //| , Person[false][false][false][false], Person[false][false][false][false], Pe
                                                  //| rson[false][false][false][false], Person[false][false][false][false], Person
                                                  //| [false][false][false][false], Person[false][false][false][false], Person[fal
                                                  //| se][false][false][false], Person[false][false][false][false], Person[false][
                                                  //| false][false][false], Person[false][false][false][false], Person[false][fals
                                                  //| e][false][false], Person[false][false][false][false], Person[false][false][f
                                                  //| alse][false], Person[false][false][false][false], Person[false][false][false
                                                  //| ][false], Person[false][false][false][false], Person[false][false][false][fa
                                                  //| lse], Person[false][false][false][false], Person[false][false][false][false]
                                                  //| , Person[false][false][false][false], Person[false][false][false][false], Pe
                                                  //| rson[false][false][false][false], Person[false][false][false][false], Person
                                                  //| [false][false][false][false], Person[false][false][false][false], Person[fal
                                                  //| se][false][false][false], Person[false][false][false][false], Person[false][
                                                  //| false][false][false], Person[false][false][false][false], Person[false][fals
                                                  //| e][false][false], Person[false][false][false][false], Person[false][false][f
                                                  //| alse][false], Person[false][false][false][false], Person[false][false][false
                                                  //| ][false], Person[false][false][false][false], Person[false][false][false][fa
                                                  //| lse], Person[false][false][false][false], Person[false][false][false][false]
                                                  //| , Person[false][false][false][false], Person[false][false][false][false], Pe
                                                  //| rson[false][false][false][false], Person[false][false][false][false], Person
                                                  //| [false][false][false][false], Person[false][false][false][false], Person[fal
                                                  //| se][false][false][false], Person[false][false][false][false], Person[false][
                                                  //| false][false][false], Person[false][false][false][false], Person[false][fals
                                                  //| e][false][false], Person[false][false][false][false], Person[false][false][f
                                                  //| alse][false], Person[false][false][false][false], Person[false][false][false
                                                  //| ][false], Person[false][false][false][false], Person[false][false][false][fa
                                                  //| lse], Person[false][false][false][false], Person[false][false][false][false]
                                                  //| , Person[false][false][false][false], Person[false][false][false][false], Pe
                                                  //| rson[false][false][false][false], Person[false][false][false][false], Person
                                                  //| [false][false][false][false], Person[false][false][false][false], Person[fal
                                                  //| se][false][false][false], Person[false][false][false][false], Person[false][
                                                  //| false][false][false], Person[false][false][false][false], Person[false][fals
                                                  //| e][false][false], Person[false][false][false][false], Person[false][false][f
                                                  //| alse][false], Person[false][false][false][false], Person[false][false][false
                                                  //| ][false], Person[false][false][false][false], Person[false][false][false][fa
                                                  //| lse], Person[false][false][false][false], Person[false][false][false][false]
                                                  //| , Person[false][false][false][false], Person[false][false][false][false], Pe
                                                  //| rson[false][false][false][false], Person[false][false][false][false], Person
                                                  //| [false][false][false][false], Person[false][false][false][false], Person[fal
                                                  //| se][false][false][false], Person[false][false][false][false], Person[false][
                                                  //| false][false][false], Person[false][false][false][false], Person[false][fals
                                                  //| e][false][false], Person[false][false][false][false], Person[false][false][f
                                                  //| alse][false], Person[false][false][false][false], Person[false][false][false
                                                  //| ][false], Person[false][false][false][false], Person[false][false][false][fa
                                                  //| lse], Person[false][false][false][false], Person[false][false][false][false]
                                                  //| , Person[false][false][false][false], Person[false][false][false][false], Pe
                                                  //| rson[false][false][false][false], Person[false][false][false][false], Person
                                                  //| [false][false][false][false], Person[false][false][false][false], Person[fal
                                                  //| se][false][false][false], Person[false][false][false][false], Person[false][
                                                  //| false][false][false], Person[false][false][false][false], Person[false][fals
                                                  //| e][false][false], Person[false][false][false][false], Person[false][false][f
                                                  //| alse][false], Person[false][false][false][false], Person[false][false][false
                                                  //| ][false], Person[false][false][false][false], Person[false][false][false][fa
                                                  //| lse], Person[false][false][false][false], Person[false][false][false][false]
                                                  //| , Person[false][false][false][false], Person[false][false][false][false], Pe
                                                  //| rson[false][false][false][false], Person[false][false][false][false], Person
                                                  //| [false][false][false][false], Person[false][false][false][false], Person[fal
                                                  //| se][false][false][false], Person[false][false][false][false], Person[false][
                                                  //| false][false][false], Person[false][false][false][false], Person[false][fals
                                                  //| e][false][false], Person[false][false][false][false], Person[false][false][f
                                                  //| alse][false], Person[false][false][false][false], Person[false][false][false
                                                  //| ][false], Person[false][false][false][false], Person[false][false][false][fa
                                                  //| lse], Person[false][false][false][false], Person[false][false][false][false]
                                                  //| , Person[false][false][false][false], Person[false][false][false][false], Pe
                                                  //| rson[false][false][false][false], Person[false][false][false][false], Person
                                                  //| [false][false][false][false], Person[false][false][false][false], Person[fal
                                                  //| se][false][false][false], Person[false][false][false][false], Person[false][
                                                  //| false][false][false], Person[false][false][false][false], Person[false][fals
                                                  //| e][false][false], Person[false][false][false][false], Person[false][false][f
                                                  //| alse][false], Person[false][false][false][false], Person[false][false][false
                                                  //| ][false], Person[false][false][false][false], Person[false][false][false][fa
                                                  //| lse], Person[false][false][false][false], Person[false][false][false][false]
                                                  //| , Person[false][false][false][false], Person[false][false][false][false], Pe
                                                  //| rson[false][false][false][false], Person[false][false][false][false], Person
                                                  //| [false][false][false][false], Person[false][false][false][false], Person[fal
                                                  //| se][false][false][false], Person[false][false][false][false], Person[false][
                                                  //| false][false][false], Person[false][false][false][false], Person[false][fals
                                                  //| e][false][false], Person[false][false][false][false], Person[false][false][f
                                                  //| alse][false], Person[false][false][false][false], Person[false][false][false
                                                  //| ][false], Person[false][false][false][false], Person[false][false][false][fa
                                                  //| lse], Person[false][false][false][false], Person[false][false][false][false]
                                                  //| , Person[false][false][false][false], Person[false][false][false][false], Pe
                                                  //| rson[false][false][false][false], Person[false][false][false][false], Person
                                                  //| [false][false][false][false], Person[false][false][false][false], Person[fal
                                                  //| se][false][false][false], Person[false][false][false][false], Person[false][
                                                  //| false][false][false], Person[false][false][false][false], Person[false][fals
                                                  //| e][false][false], Person[false][false][false][false], Person[false][false][f
                                                  //| alse][false], Person[false][false][false][false], Person[false][false][false
                                                  //| ][false], Person[false][false][false][false], Person[false][false][false][fa
                                                  //| lse], Person[false][false][false][false], Person[false][false][false][false]
                                                  //| , Person[false][false][false][false], Person[false][false][false][false], Pe
                                                  //| rson[false][false][false][false], Person[false][false][false][false], Person
                                                  //| [false][false][false][false], Person[false][false][false][false], Person[fal
                                                  //| se][false][false][false], Person[false][false][false][false], Person[false][
                                                  //| false][false][false], Person[false][false][false][false], Person[false][fals
                                                  //| e][false][false], Person[false][false][false][false], Person[false][false][f
                                                  //| alse][false], Person[false][false][false][false], Person[false][false][false
                                                  //| ][false], Person[false][false][false][false], Person[false][false][false][fa
                                                  //| lse], Person[false][false][false][false], Person[false][false][false][false]
                                                  //| , Person[false][false][false][false], Person[false][false][false][false], Pe
                                                  //| rson[false][false][false][false], Person[false][false][false][false], Person
                                                  //| [false][false][false][false], Person[false][false][false][false], Person[fal
                                                  //| se][false][false][false], Person[false][false][false][false], Person[false][
                                                  //| false][false][false], Person[false][false][false][false], Person[false][fals
                                                  //| e][false][false], Person[false][false][false][false], Person[false][false][f
                                                  //| alse][false], Person[false][false][false][false], Person[false][false][false
                                                  //| ][false], Person[false][false][false][false], Person[false][false][false][fa
                                                  //| lse], Person[false][false][false][false], Person[false][false][false][false]
                                                  //| , Person[false][false][false][false], Person[false][false][false][false], Pe
                                                  //| rson[false][false][false][false], Person[false][false][false][false], Person
                                                  //| [false][false][false][false], Person[false][false][false][false], Person[fal
                                                  //| se][false][false][false], Person[false][false][false][false], Person[false][
                                                  //| false][false][false], Person[false][false][false][false], Person[false][fals
                                                  //| e][false][false], Person[false][false][false][false], Person[false][false][f
                                                  //| alse][false], Person[false][false][false][false], Person[false][false][false
                                                  //| ][false], Person[false][false][false][false], Person[false][false][false][fa
                                                  //| lse], Person[false][false][false][false], Person[false][false][false][false]
                                                  //| , Person[false][false][false][false], Person[false][false][false][false], Pe
                                                  //| rson[false][false][false][false], Person[false][false][false][false], Person
                                                  //| [false][false][false][false], Person[false][false][false][false], Person[fal
                                                  //| se][false][false][false], Person[false][false][false][false])
 
  agenda                                          //> res1: simulations.EpidemySimulatorWs.Agenda = List(WorkItem(1,<function0>), 
                                                  //| WorkItem(1,<function0>), WorkItem(1,<function0>), WorkItem(1,<function0>), W
                                                  //| orkItem(1,<function0>), WorkItem(1,<function0>), WorkItem(1,<function0>), Wo
                                                  //| rkItem(1,<function0>), WorkItem(1,<function0>), WorkItem(1,<function0>), Wor
                                                  //| kItem(1,<function0>), WorkItem(1,<function0>), WorkItem(1,<function0>), Work
                                                  //| Item(1,<function0>), WorkItem(1,<function0>), WorkItem(1,<function0>), WorkI
                                                  //| tem(1,<function0>), WorkItem(1,<function0>), WorkItem(1,<function0>), WorkIt
                                                  //| em(1,<function0>), WorkItem(1,<function0>), WorkItem(1,<function0>), WorkIte
                                                  //| m(1,<function0>), WorkItem(1,<function0>), WorkItem(1,<function0>), WorkItem
                                                  //| (1,<function0>), WorkItem(1,<function0>), WorkItem(1,<function0>), WorkItem(
                                                  //| 1,<function0>), WorkItem(1,<function0>), WorkItem(1,<function0>), WorkItem(1
                                                  //| ,<function0>), WorkItem(1,<function0>), WorkItem(1,<function0>), WorkItem(1,
                                                  //| <function0>), WorkItem(1,<function0>), WorkItem(1,<function0>), WorkItem(1,<
                                                  //| function0>), WorkItem(1,<function0>), WorkItem(1,<function0>), WorkItem(1,<f
                                                  //| unction0>), WorkItem(1,<function0>), WorkItem(1,<function0>), WorkItem(1,<fu
                                                  //| nction0>), WorkItem(1,<function0>), WorkItem(1,<function0>), WorkItem(1,<fun
                                                  //| ction0>), WorkItem(1,<function0>), WorkItem(1,<function0>), WorkItem(1,<func
                                                  //| tion0>), WorkItem(1,<function0>), WorkItem(1,<function0>), WorkItem(1,<funct
                                                  //| ion0>), WorkItem(1,<function0>), WorkItem(1,<function0>), WorkItem(1,<functi
                                                  //| on0>), WorkItem(1,<function0>), WorkItem(1,<function0>), WorkItem(1,<functio
                                                  //| n0>), WorkItem(1,<function0>), WorkItem(1,<function0>), WorkItem(1,<function
                                                  //| 0>), WorkItem(1,<function0>), WorkItem(1,<function0>), WorkItem(1,<function0
                                                  //| >), WorkItem(1,<function0>), WorkItem(2,<function0>), WorkItem(2,<function0>
                                                  //| ), WorkItem(2,<function0>), WorkItem(2,<function0>), WorkItem(2,<function0>)
                                                  //| , WorkItem(2,<function0>), WorkItem(2,<function0>), WorkItem(2,<function0>),
                                                  //|  WorkItem(2,<function0>), WorkItem(2,<function0>), WorkItem(2,<function0>), 
                                                  //| WorkItem(2,<function0>), WorkItem(2,<function0>), WorkItem(2,<function0>), W
                                                  //| orkItem(2,<function0>), WorkItem(2,<function0>), WorkItem(2,<function0>), Wo
                                                  //| rkItem(2,<function0>), WorkItem(2,<function0>), WorkItem(2,<function0>), Wor
                                                  //| kItem(2,<function0>), WorkItem(2,<function0>), WorkItem(2,<function0>), Work
                                                  //| Item(2,<function0>), WorkItem(2,<function0>), WorkItem(2,<function0>), WorkI
                                                  //| tem(2,<function0>), WorkItem(2,<function0>), WorkItem(2,<function0>), WorkIt
                                                  //| em(2,<function0>), WorkItem(2,<function0>), WorkItem(2,<function0>), WorkIte
                                                  //| m(2,<function0>), WorkItem(2,<function0>), WorkItem(2,<function0>), WorkItem
                                                  //| (2,<function0>), WorkItem(2,<function0>), WorkItem(2,<function0>), WorkItem(
                                                  //| 2,<function0>), WorkItem(2,<function0>), WorkItem(2,<function0>), WorkItem(2
                                                  //| ,<function0>), WorkItem(2,<function0>), WorkItem(2,<function0>), WorkItem(2,
                                                  //| <function0>), WorkItem(2,<function0>), WorkItem(2,<function0>), WorkItem(2,<
                                                  //| function0>), WorkItem(2,<function0>), WorkItem(2,<function0>), WorkItem(2,<f
                                                  //| unction0>), WorkItem(2,<function0>), WorkItem(2,<function0>), WorkItem(2,<fu
                                                  //| nction0>), WorkItem(2,<function0>), WorkItem(3,<function0>), WorkItem(3,<fun
                                                  //| ction0>), WorkItem(3,<function0>), WorkItem(3,<function0>), WorkItem(3,<func
                                                  //| tion0>), WorkItem(3,<function0>), WorkItem(3,<function0>), WorkItem(3,<funct
                                                  //| ion0>), WorkItem(3,<function0>), WorkItem(3,<function0>), WorkItem(3,<functi
                                                  //| on0>), WorkItem(3,<function0>), WorkItem(3,<function0>), WorkItem(3,<functio
                                                  //| n0>), WorkItem(3,<function0>), WorkItem(3,<function0>), WorkItem(3,<function
                                                  //| 0>), WorkItem(3,<function0>), WorkItem(3,<function0>), WorkItem(3,<function0
                                                  //| >), WorkItem(3,<function0>), WorkItem(3,<function0>), WorkItem(3,<function0>
                                                  //| ), WorkItem(3,<function0>), WorkItem(3,<function0>), WorkItem(3,<function0>)
                                                  //| , WorkItem(3,<function0>), WorkItem(3,<function0>), WorkItem(3,<function0>),
                                                  //|  WorkItem(3,<function0>), WorkItem(3,<function0>), WorkItem(3,<function0>), 
                                                  //| WorkItem(3,<function0>), WorkItem(3,<function0>), WorkItem(3,<function0>), W
                                                  //| orkItem(3,<function0>), WorkItem(3,<function0>), WorkItem(3,<function0>), Wo
                                                  //| rkItem(3,<function0>), WorkItem(3,<function0>), WorkItem(3,<function0>), Wor
                                                  //| kItem(3,<function0>), WorkItem(3,<function0>), WorkItem(3,<function0>), Work
                                                  //| Item(3,<function0>), WorkItem(3,<function0>), WorkItem(3,<function0>), WorkI
                                                  //| tem(3,<function0>), WorkItem(3,<function0>), WorkItem(3,<function0>), WorkIt
                                                  //| em(3,<function0>), WorkItem(3,<function0>), WorkItem(3,<function0>), WorkIte
                                                  //| m(3,<function0>), WorkItem(3,<function0>), WorkItem(4,<function0>), WorkItem
                                                  //| (4,<function0>), WorkItem(4,<function0>), WorkItem(4,<function0>), WorkItem(
                                                  //| 4,<function0>), WorkItem(4,<function0>), WorkItem(4,<function0>), WorkItem(4
                                                  //| ,<function0>), WorkItem(4,<function0>), WorkItem(4,<function0>), WorkItem(4,
                                                  //| <function0>), WorkItem(4,<function0>), WorkItem(4,<function0>), WorkItem(4,<
                                                  //| function0>), WorkItem(4,<function0>), WorkItem(4,<function0>), WorkItem(4,<f
                                                  //| unction0>), WorkItem(4,<function0>), WorkItem(4,<function0>), WorkItem(4,<fu
                                                  //| nction0>), WorkItem(4,<function0>), WorkItem(4,<function0>), WorkItem(4,<fun
                                                  //| ction0>), WorkItem(4,<function0>), WorkItem(4,<function0>), WorkItem(4,<func
                                                  //| tion0>), WorkItem(4,<function0>), WorkItem(4,<function0>), WorkItem(4,<funct
                                                  //| ion0>), WorkItem(4,<function0>), WorkItem(4,<function0>), WorkItem(4,<functi
                                                  //| on0>), WorkItem(4,<function0>), WorkItem(4,<function0>), WorkItem(4,<functio
                                                  //| n0>), WorkItem(4,<function0>), WorkItem(4,<function0>), WorkItem(4,<function
                                                  //| 0>), WorkItem(4,<function0>), WorkItem(4,<function0>), WorkItem(4,<function0
                                                  //| >), WorkItem(4,<function0>), WorkItem(4,<function0>), WorkItem(4,<function0>
                                                  //| ), WorkItem(4,<function0>), WorkItem(4,<function0>), WorkItem(4,<function0>)
                                                  //| , WorkItem(4,<function0>), WorkItem(4,<function0>), WorkItem(4,<function0>),
                                                  //|  WorkItem(4,<function0>), WorkItem(4,<function0>), WorkItem(4,<function0>), 
                                                  //| WorkItem(5,<function0>), WorkItem(5,<function0>), WorkItem(5,<function0>), W
                                                  //| orkItem(5,<function0>), WorkItem(5,<function0>), WorkItem(5,<function0>), Wo
                                                  //| rkItem(5,<function0>), WorkItem(5,<function0>), WorkItem(5,<function0>), Wor
                                                  //| kItem(5,<function0>), WorkItem(5,<function0>), WorkItem(5,<function0>), Work
                                                  //| Item(5,<function0>), WorkItem(5,<function0>), WorkItem(5,<function0>), WorkI
                                                  //| tem(5,<function0>), WorkItem(5,<function0>), WorkItem(5,<function0>), WorkIt
                                                  //| em(5,<function0>), WorkItem(5,<function0>), WorkItem(5,<function0>), WorkIte
                                                  //| m(5,<function0>), WorkItem(5,<function0>), WorkItem(5,<function0>), WorkItem
                                                  //| (5,<function0>), WorkItem(5,<function0>), WorkItem(5,<function0>), WorkItem(
                                                  //| 5,<function0>), WorkItem(5,<function0>), WorkItem(5,<function0>), WorkItem(5
                                                  //| ,<function0>), WorkItem(5,<function0>), WorkItem(5,<function0>), WorkItem(5,
                                                  //| <function0>), WorkItem(5,<function0>), WorkItem(5,<function0>), WorkItem(5,<
                                                  //| function0>), WorkItem(5,<function0>), WorkItem(5,<function0>), WorkItem(5,<f
                                                  //| unction0>), WorkItem(5,<function0>), WorkItem(5,<function0>), WorkItem(5,<fu
                                                  //| nction0>), WorkItem(5,<function0>), WorkItem(5,<function0>), WorkItem(5,<fun
                                                  //| ction0>), WorkItem(5,<function0>), WorkItem(5,<function0>), WorkItem(5,<func
                                                  //| tion0>), WorkItem(5,<function0>), WorkItem(5,<function0>), WorkItem(5,<funct
                                                  //| ion0>), WorkItem(5,<function0>), WorkItem(5,<function0>), WorkItem(5,<functi
                                                  //| on0>), WorkItem(5,<function0>), WorkItem(5,<function0>), WorkItem(5,<functio
                                                  //| n0>), WorkItem(5,<function0>), WorkItem(5,<function0>), WorkItem(5,<function
                                                  //| 0>), WorkItem(5,<function0>), WorkItem(5,<function0>), WorkItem(5,<function0
                                                  //| >), WorkItem(5,<function0>), WorkItem(5,<function0>), WorkItem(5,<function0>
                                                  //| ), WorkItem(5,<function0>), WorkItem(5,<function0>), WorkItem(5,<function0>)
                                                  //| , WorkItem(5,<function0>))
 
  run                                             //> *** New propagation ***|
  agenda
  //addAction
  persons
  rooms
  //persons.head.die()
  //persons.tail.head.infect()
  var p = persons.head
              
  p.makeInfectionEv
  agenda
  run
  persons
  rooms
  //val p = new Person(1)
  //persons map (_.die)
  //persons
  //rooms
  val locations = List((0,0), (7,7), (8,8), (3,3), (3,3), (3,3), (3,3), (3,3), (3,3))
  for (loc <- locations) yield loc._1
  //locations map (p.nextRoomAddress(_))
  
  
  
}