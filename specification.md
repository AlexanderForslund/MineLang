# MineLANG Language Specification

## Instructions

| **Type**  | **Encoding**                          |
|:----------|:--------------------------------------|
| Register  | `op<7:5>, rs<4:3>, rt<2:1>, imm<0>`   |
| Jump      | `op<7:5>, addr<4:0>`                  |
| Special   | `op<7:5>`                             |


### Register Instructions
000 001 010 011
100 101 110 111

| **Instruction**   | **Syntax**            | **Description**                                                                       |
|:------------------|:----------------------|:--------------------------------------------------------------------------------------|
| `add  rt rs imm`  | `Nether Star`         | `rt = rt (operator) rs` where `operator = (imm == 0: +), (imm == 1: -)`               |
| `addi rt imm`     | `Fern`                | `rt = rt + imm` where `-4 <= imm <= 3`                                                  |
| `set  rt imm`     | `Arrow`               | `rt = imm` where `-4 <= imm <= 3`                                                                            |
| `jeq  rt rs imm`  | `Redstone Comparator` | Jump one instruction if `(rt == rs && imm == 0) or (rt != rs && imm == 1)`            |
| `input rt`        | `Name Tag`            | Set rt to value of integer in book placed on spawned lectern (Don't sign the book)    |
| `print rt`        | `Oak Sign`            | Write value of rt in chat                                                             |

*Papers are used for `imm`, name them to an 32-bit signed integer by using an anvil.*

### Jump Instructions

| **Instruction**   | **Description**                             |
|:------------------|:--------------------------------------------|
| `jump imm`        | Jump `imm` instructions (NOT ARGUMENTS), where `-16 <= imm <= 15`   |

### Special Instructions

| **Instruction**   | **Syntax**      | **Description**                                                                     |
|:------------------|:----------------|:------------------------------------------------------------------------------------|
| `exit`            | `Dragon Head`   | Terminate program                                                                   |

## Registers

    There are four registers that can hold one i32 value each. They are annotated by a block crafted by ingots. (Block of Iron, Block of Gold, Block of Diamond, Block of Netherite)

    - Block of Netherite    = $0 (zero registry, always has value 0)
    - Block of Iron         = $r0
    - Block of Gold         = $r1                   
    - Block of Diamond      = $r2