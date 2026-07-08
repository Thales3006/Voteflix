{
  description = "Movie reviewer client-server application";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-25.05";

  outputs = { self, nixpkgs }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs { inherit system; };

      javafxLibs = with pkgs; [
        gtk3
        glib
        libGL
        xorg.libXxf86vm
        xorg.libXrender
        xorg.libXtst
        xorg.libXi
        xorg.libX11
        xorg.libXext
        xorg.libXcursor
        xorg.libXrandr
        xorg.libXinerama
      ];

      javaEnv = ''
        export JAVA_HOME=${pkgs.jdk23}
        export PATH=${pkgs.jdk23}/bin:${pkgs.maven}/bin:$PATH
      '';

      mkClientApp = pkgs.writeShellScript "run-client" ''
        ${javaEnv}
        export LD_LIBRARY_PATH=${pkgs.lib.makeLibraryPath javafxLibs}

        mvn -pl common install -DskipTests
        mvn -pl client javafx:run
      '';

      mkServerApp = pkgs.writeShellScript "run-server" ''
        ${javaEnv}

        mvn -pl common install -DskipTests
        mvn -pl server exec:java
      '';

    in {

      devShells.${system}.default = pkgs.mkShell {
        buildInputs = with pkgs; [
          jdk23
          maven
          scenebuilder
        ] ++ javafxLibs;

        shellHook = ''
          export LD_LIBRARY_PATH=${pkgs.lib.makeLibraryPath javafxLibs}
        '';
      };

      apps.${system} = {

        default = {
          type = "app";
          program = toString mkClientApp;
        };

        client = {
          type = "app";
          program = toString mkClientApp;
        };

        server = {
          type = "app";
          program = toString mkServerApp;
        };

      };
    };
}
