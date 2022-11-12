# Check if item is in chest at coordinates
execute if data block 0 56 0 Items[{Slot:0b,id:"minecraft:golden_pickaxe",Count:1b,tag:{Damage:0}}] run say "fortnite"



# Binary to decimal converter
execute if data block -12 56 3 Items[{Slot:0b,id:"minecraft:torch",Count:1b}] run item replace block 0 56 0 container.0 with minecraft:carved_pumpkin{Enchantments:[{id:"minecraft:efficiency",lvl:0}]}



item replace block 0 56 0 0 with minecraft:torch 1