ADME library - Android Database Made Easy
=======

This library is a work in progress but I already use it in some project.
If you stumble on it feel free to try it out but be aware the API may change before the first stable release.

Openly inspired by ORMLite but aimed directly to Android development.

The library is not an ORM.

Implemented features:

  * Entity annotations for tables
  * Field annotations for columns
  * Index / Constraint annotation for unique constraints and indexing
  * Foreign key basic support
  * Automatic generation of SQL for creating table, creating indexes, dropping tables
  * Automatic generation of ContentValues objects from class instances for database inserts / update
  * Support for storing in a column of the database your custom Datatype (custom ADMESerializer)


Currently missing features I really think should be implemented before release:

  * Support for usage of custom getter/setters in entity fields
  * Support for automatic conversion from a Cursor to a class instance (wrapping a Cursor maybe)
  * Gradle AAR on maven central
  * Implement a Demo project
  * Markdown Documentation

Ideas for features I would like to implement:

  * Include annotation to include the columns of another entity, optionally with a prefix for column names
  * Annotation processor at compile time to generate more boilerplate code (ex. ContentProviders)

