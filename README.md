# Voteflix

A client-server movie review application.

## Description

This project is a movie review system originally built for the Distributed Systems university class as a practical implementation of the client-server architecture and the application protocol created during the course.

The project uses Java 23 as the main language, with JavaFX (GUI) for both the client and the server, as originally specified for the class.

## Dependencies

The project uses Maven for Java dependencies and Nix for an easier build/run interface and general management.

The recommended way to run the project is to install and use Nix, allowing it to manage the project dependencies.

## Running

The project can be run using pure Maven, but it is recommended to use Nix to run the project more easily (make sure flakes are enabled):

```bash
# running the server
nix run <project_folder_path>#server

# running the client
nix run <project_folder_path>#client
```