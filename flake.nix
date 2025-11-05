{
  description = "Spring Boot jqwik Demo";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-24.11";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
      in
      {
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            jdk21
            maven
          ];

          shellHook = ''
            echo "Spring Boot jqwik Demo Development Environment"
            echo "Java version:"
            java --version
            echo "Maven version:"
            mvn --version
          '';
        };
      }
    );
}
