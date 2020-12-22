# kurl
libcurl bindings for Kotlin/Native

## Goal
The goal of this project is to create easy to use, kotlin-style bindings (not just interop stubs) that provide low-level access as well by exposing the C pointers
to libcurl objects.

## Implemented features
  - Basic easy_handle operations: perform, cleanup, change url, as well as:
    - Change user-agent
    - Enable/Disable verbose mode
    - Enable/Disable certificate verification
    - Enable/Disable headers
    - Set cookie values 
    - Encode String to URL
  - Builder syntax for easy handles as well as regular object usage.
  - "Use" method to automatically dispose the handle after usage.

## Requirements
To compile this library and projects depending on it, you must have libcurl installed in your system.
