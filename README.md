# PDFTools

Simple Kotlin Console app for some PDF Processing.

## Supported Actions:

- Count PDF Pages
- Remove Pages from PDF to a new output
- Merge PDF files into a new output
- Compress PDF
- Generate a PDF output without password from input *(using the correct password to do it)*

## Usage

```
java PDFTools.jar <action> <inputs> <output> <options>
```

### Examples

#### Count PDF Pages

```
java PDFTools.jar page-number input-file-path.pdf
```

#### Remove Pages from PDF

After the action, specify input, output and the pages to remove (separated by commas)

```
java PDFTools.jar remove-pages input-file-path.pdf output-file-path.pdf 1,2,3,5
```

#### Merge PDF files

After the action, specify all files to be merged. The last path will always be the output path

```
java PDFTools.jar merge-files file1.pdf file2.pdf file3.pdf output.pdf
```

#### Compress PDF

After the action, just specify input and output files

```
java PDFTools.jar compress input-file-path.pdf output-file-path.pdf
```

#### Unlock PDF

With the password, will generate another output without protection

```
java PDFTools.jar unlock-document input-file-path.pdf output-file-path.pdf <password>
```

**PS:** *The compression is limited to reduce the jpeg quality to 50% and scaling
down the image in 20%. Being able to change these settings is a feature for the next versions*

**PS2**: *Compression will try to compress only the images, leaving texts and other objects
as they are without turning everything into an image*


