package app.dream2gether.cli

private const val USAGE = """d2g: dream2gether laptop tool (everything runs offline)

Usage:
  d2g init-space <dir>                 (coming in B-008)
  d2g process <audio.m4a> --space <dir> (coming in B-008)
  d2g eval                              (coming in B-010)
"""

fun main(args: Array<String>) {
    print(USAGE)
}
