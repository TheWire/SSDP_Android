# SSDP_Android

Android Kotlin library for finding simple service detection protocol (SSDP) devices on local network.

## Basic Usage

```
val ssdp = SSDP(applicationContext)

CoroutineScope(Dispatchers.IO).launch {

  ssdp.lockMulticast()
  
  ssdp.discover(probe = true, duration = 60).collect { message ->
  
    when(message) {
      DiscoverMessage.Discovering -> println("Discovering")
      is DiscoverMessage.Error -> println("Error")
      is DiscoverMessage.Message -> println(message)
    }
    
  }
  
  ssdp.releaseMulticast()
  
}

```
