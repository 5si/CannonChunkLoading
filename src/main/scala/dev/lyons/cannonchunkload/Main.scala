package dev.lyons.cannonchunkload

import me.suicidalkids.SKSpigot.events.entity.TNTDispenseEvent
import org.bukkit.Chunk
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.event.{EventHandler, Listener}

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class Main extends JavaPlugin with Listener {

  val chunks = new ConcurrentHashMap[Chunk, AtomicLong]()
  var serverTick = 0L

  override def onEnable(): Unit = {
    getServer.getPluginManager.registerEvents(this, this)
    getServer.getScheduler.scheduleSyncRepeatingTask(this, () => {
      serverTick += 1
    }, 0, 1)
    getServer.getScheduler.scheduleSyncRepeatingTask(this, () => {
      if (!chunks.isEmpty) {
        chunks.keySet().forEach(chunk => {
          if (serverTick - chunks.get(chunk).get() > 240) {
            chunks.remove(chunk)
          }
        })
      }
    }, 10, 10)
  }

  @EventHandler(ignoreCancelled = true)
  def tntSpawn(e: TNTDispenseEvent): Unit = {
    val chunk = e.getTNTPrimed.getLocation.clone().getChunk
    if (!chunks.containsKey(chunk)) {
      chunks.put(chunk, new AtomicLong(serverTick))
    } else {
      chunks.get(chunk).set(serverTick)
    }
  }

  @EventHandler(ignoreCancelled = true)
  def chunkUnload(e: ChunkUnloadEvent): Unit = {
    if (chunks.containsKey(e.getChunk)) {
      e.setCancelled(true)
    }
  }

}