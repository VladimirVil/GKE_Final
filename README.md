# GKE_Final
Version 1.0.0
The following implementation allows a fixed group of participants agree on a shared symmetric secret key whereby
all of the participate in its calculation

Implemented for 4 participants (Alice, Bob, Claire, Eric)

Validated through /GKE/test/basicTestsSimpleInterfaceVersion/GKETestsV3.java

Generator and prime are used according to RFC3526, id 16(Modular Exponential DH Group 4096)

Calculation done using the class BigInteger.
