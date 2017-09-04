# EPFViewer

## Quickstart

* Python 3 (PyQt5 is Py3)
* Python pip3 dependencies:

```
pip3 install pyqt5
pip3 install pillow
pip3 install struct

```

* Running the script
```python3 epf_viewer.py```

NexusTK EPF Viewer for viewing and exporting EPF/TBL/PAL files.

## file_reader module

This module contains readers for EPF, TBL, and PAL files.

### File Structures

#### TBL File Structure

int tile_count                     (4 bytes)
int palette_count                  (4 bytes)
byte[3] unknown                    (3 bytes)
short[tile_count] palette_indicies (2 * tile_count bytes)

#### PAL File Structure

byte[32] header    (32 bytes)
color[256] palette (1024 bytes)

struct {
  byte blue    (1 byte)
  byte green   (1 byte)
  byte red     (1 byte)
  byte padding (1 byte)
} color (4 bytes)

#### EPF File Structure

short tile_count
short width
short height
short unknown
int pixel_data_length
byte[pixel_data_length] pixel_data
tile_entry[tile_count] tile_entries

struct {
  int unknown
  short width
  short height
  int pixel_data_offset
  int unknown_offset
} table_entry

## epf_viewer GUI (PyQt5)

Features:

Since this exports NexusTK EPF/PAL/TBL files, the EPFViewer is coded to
reference TileA, TileB, TileC format - this can be modified for custom projects.

* Export A Tiles to Bitmaps
* Export B Tiles to Bitmaps
* Export C Tiles to Bitmaps

* Export individual Tiles to Bitmap (Right-Click)