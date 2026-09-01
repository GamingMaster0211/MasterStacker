# MasterStacker
This plugin adds simple mob and spawner stacking, along with silk spawners.

The default config for MasterStacker is as follows:

```yml
#     __  ___           __            _____ __             __
#    /  |/  /___ ______/ /____  _____/ ___// /_____ ______/ /_____  _____
#   / /|_/ / __ `/ ___/ __/ _ \/ ___/\__ \/ __/ __ `/ ___/ //_/ _ \/ ___/
#  / /  / / /_/ (__  ) /_/  __/ /   ___/ / /_/ /_/ / /__/ ,< /  __/ /
# /_/  /_/\__,_/____/\__/\___/_/   /____/\__/\__,_/\___/_/|_|\___/_/
#
#    ______            _____
#   / ____/___  ____  / __(_)___ _
#  / /   / __ \/ __ \/ /_/ / __ `/
# / /___/ /_/ / / / / __/ / /_/ /
# \____/\____/_/ /_/_/ /_/\__, /
#                        /____/

# This is the config file for MasterStacker. Documentation is on the Modrinth page.

spawners:
  # Maximum number of normal spawners represented by one physical spawner block.
  # Default max stack size is 100
  max-stack-size: 100

  hologram:
    enabled: true

    # MiniMessage formatting is supported.
    #
    # Placeholders:
    # %amount% = number of spawners
    # %mob%    = mob name
    # Default:
    # 3x Iron Golem Spawner
    format: "<aqua>%amount%x</aqua> <white>%mob% Spawner</white>"

    # Height above the spawner block.
    # Default height is 1.5
    height: 1.5

  item:
    # Name displayed on collected spawners.
    # Example:
    # 1x Zombie Spawner
    # 3x Iron Golem Spawner
    format: "<aqua>%amount%x</aqua> <white>%mob% Spawner</white>"


mob-stacking:
  enabled: true

  # Default cooldown is 100 ticks.
  stacking-cooldown-ticks: 100

  # Default max stack size is 100
  max-stack-size: 100

  # These entities are never stacked.
  disabled-mobs:
    - BEE
    - VILLAGER

  multi-kill:
    enabled: true

    # The enchantment itself acts as the permission to multi-kill.
    # The level does not matter.
    # Default multi-kill enchantment is minecraft:sweeping_edge
    enchantment: "minecraft:sweeping_edge"

    # Number of mobs killed from the stack if the player
    # has the configured enchantment.
    # Default amount is 2
    amount: 2


permissions:
  silk-touch: "masterstacker.silk"
  admin: "masterstacker.admin"
```
