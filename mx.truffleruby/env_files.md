Here is how the various env files relate to each other:
* `jvm`
  * `jvm-ce`: + GraalVM CE Compiler
    * `jvm-ce-ntl`: + native toolchain launchers
      * `jvm-ce-libgraal`: + libgraal
      * `native`: + librubyvm
  * `jvm-ee`: + Oracle GraalVM Compiler
    * `jvm-ee-ntl`: + native toolchain launchers
      * `jvm-ee-libgraal`: + libgraal
      * `native-ee`: + librubyvm
        * `native-ee-g1`: + Native Image G1
  * `jvm-gu`: + Graal Updater
  * `jvm-js`: + Graal.js
  * `jvm-py`: + GraalPython
