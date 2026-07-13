#!/bin/bash
sed -i '574,578d' app/src/main/java/com/example/MainActivity.kt
sed -i '573a \                                            }\n                                        }' app/src/main/java/com/example/MainActivity.kt
