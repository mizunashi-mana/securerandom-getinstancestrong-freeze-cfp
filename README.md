# SecureRandom.getInstanceStrong() を気軽に使ってはいけない

Run container (finish by Ctrl+D):

```bash
$ docker build -t securerandom-freeze-cfp-image .
$ docker run --name securerandom-freeze-cfp -it securerandom-freeze-cfp-image
+ '[' true = true ']'
+ rm /dev/random
+ mkfifo /dev/random
+ echo 'PID: 9'
PID: 9
+ exec tee /dev/random
+ java Main
Start thread

```

Dump thread:

```bash
$ docker exec securerandom-freeze-cfp jstack 9
...
"Thread-0" #13 prio=5 os_prio=0 cpu=23.44ms elapsed=12.94s tid=0x0000ffff88172de0 nid=0x1c runnable  [0x0000ffff5cdec000]
   java.lang.Thread.State: RUNNABLE
        at java.io.FileInputStream.readBytes(java.base@17.0.6/Native Method)
        at java.io.FileInputStream.read(java.base@17.0.6/FileInputStream.java:276)
        at java.io.FilterInputStream.read(java.base@17.0.6/FilterInputStream.java:132)
        at sun.security.provider.NativePRNG$RandomIO.readFully(java.base@17.0.6/NativePRNG.java:425)
        at sun.security.provider.NativePRNG$RandomIO.getMixRandom(java.base@17.0.6/NativePRNG.java:405)
        - locked <0x000000008cf91998> (a java.lang.Object)
        at sun.security.provider.NativePRNG$RandomIO.implNextBytes(java.base@17.0.6/NativePRNG.java:537)
        at sun.security.provider.NativePRNG$Blocking.engineNextBytes(java.base@17.0.6/NativePRNG.java:269)
        at java.security.SecureRandom.nextBytes(java.base@17.0.6/SecureRandom.java:758)
        at DataCryptor.encrypt(DataCryptor.java:66)
        at ApiService.registerData(ApiService.java:15)
        at Server.run(Server.java:10)
        at java.lang.Thread.run(java.base@17.0.6/Thread.java:833)
...
```
