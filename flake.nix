{
  description = "JavaFX + Maven dev shell";

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
        export LD_LIBRARY_PATH=${pkgs.lib.makeLibraryPath javafxLibs}
      '';

      mkJavaApp = module: pkgs.writeShellScript "run-${module}" ''
        ${javaEnv}

        mvn -pl common install -DskipTests
        mvn -pl ${module} javafx:run
      '';

    in {

      devShells.${system}.default = pkgs.mkShell {
        buildInputs = with pkgs; [
          jdk23
          maven
        ] ++ javafxLibs;

        shellHook = ''
          export LD_LIBRARY_PATH=${pkgs.lib.makeLibraryPath javafxLibs}
        '';
      };

      apps.${system} = {
        server = {
          type = "app";
          program = toString (mkJavaApp "server");
        };

        client = {
          type = "app";
          program = toString (mkJavaApp "client");
        };
      };
    };
}