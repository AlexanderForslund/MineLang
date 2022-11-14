# MineLANG Language Specification

## Instructions

| **Type**           | **Encoding**                        |
| :----------------- | :---------------------------------- |
| Register-Immediate | `op<7:5>, rs<4:3>, rt<2:1>, imm<0>` |
| Register           | `op<7:5>, rs<4:3>`                  |
| Immediate          | `op<7:5>, rs<4:3>, imm<2:0>`        |
| Jump               | `op<7:5>, imm<4:0>`                 |
| Special            | `op<7:5>`                           |

### _Papers are used for `imm`, name them to an signed integer (following the size constraints) by using an anvil._

### Register-Immediate Instructions

| **Instruction** | **Syntax**            | **Description**                                                            |
| :-------------- | :-------------------- | :------------------------------------------------------------------------- |
| `add rt rs imm` | `Nether Star`         | `rt = rt (operator) rs` where `operator = (imm == 0: +), (imm == 1: -)`    |
| `jeq rt rs imm` | `Redstone Comparator` | Jump one instruction if `(rt == rs && imm == 0) or (rt != rs && imm == 1)` |

### Register Instructions

| **Instruction** | **Syntax** | **Description**                                                                    |
| :-------------- | :--------- | :--------------------------------------------------------------------------------- |
| `input rt`      | `Name Tag` | Set rt to value of integer in book placed on spawned lectern (Don't sign the book) |
| `print rt`      | `Oak Sign` | Write value of rt in chat                                                          |

### Immediate Instructions

| **Instruction** | **Syntax** | **Description**                        |
| :-------------- | :--------- | :------------------------------------- |
| `addi rt imm`   | `Fern`     | `rt = rt + imm` where `-4 <= imm <= 3` |
| `set rt imm`    | `Arrow`    | `rt = imm` where `-4 <= imm <= 3`      |

### Jump Instructions

| **Instruction** | **Description**                                                   |
| :-------------- | :---------------------------------------------------------------- |
| `jump imm`      | Jump `imm` instructions (NOT ARGUMENTS), where `-16 <= imm <= 15` |

### Special Instructions

| **Instruction** | **Syntax**    | **Description**   |
| :-------------- | :------------ | :---------------- |
| `exit`          | `Dragon Head` | Terminate program |

## Registers

> ### There are four registers that can hold one i32 value each. They are annotated by a block crafted by ingots. (Block of <span style="color:#ac2020">Netherite</span>, Block of <span style="color:#9A9A9A">Iron</span>, Block of <span style="color:#FECD03">Gold</span>, Block of <span style="color:#4EE2EC">Diamond</span>)
>
> - Block of <span style="color:#ac2020">Netherite</span> = $0 (zero registry, always has value 0) <!-- or this color #723232 -->
> - Block of <span style="color:#9A9A9A">Iron</span> = $r0
> - Block of <span style="color:#FECD03">Gold</span> = $r1
> - Block of <span style="color:#4EE2EC">Diamond</span> = $r2
