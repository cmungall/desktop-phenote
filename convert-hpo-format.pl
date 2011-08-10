#!/usr/bin/perl
# Convert hpo annotation files from various old formats (different columns) to new.
# Call with a directory argument (or it will just use the current directory).
# A new directory (same name with .new at the end, in same parent directory) will be
# created.  All .tab files in the old directory will be converted to the new
# human-annotation configuration and saved (with their current file names) in the
# new directory.
# New human-annotation configuration is hardcoded for now but should be parsed from
# human-annotation.cfg (fetched from svn) so as to be up-to-date.
#
# Example of a "before" file (MIM-100050.tab, first two lines):
# Disease ID	Disease Name	Gene ID	Gene Name	Genotype	Gene Symbol(s)	Phenotype ID	Phenotype Name	Entity ID	Entity Name	Quality ID	Quality Name	Add'l Entity ID	Add 'l Entity Name	Mode of inheritance ID	Mode of inheritance Name	Age of Onset ID	Age of Onset Name	Evidence	Frequency	Abnormal ID	Abnormal Name	Description	Orthologs	Pub	Date Created	
# MIM:100050	AARSKOG SYNDROME					HP:0000028												IEA							23.07.2009	
#
# That file, after conversion:
# Disease ID	Disease Name	Gene ID	Gene Name	Genotype	Gene Symbol(s)	Phenotype ID	Phenotype Name	Age of Onset ID	Age of Onset Name	Evidence ID	Evidence Name	Frequency	Sex ID	Sex Name	Negation ID	Negation Name	Description	Pub	Assigned by	Date Created
# MIM:100050	AARSKOG SYNDROME	 		 		HP:0000028	 		 	IEA	IEA	 							HPO	23.07.2009

use strict;

# Use directory specified as command-line argument, or current directory if none specified.
my $olddir = $ARGV[0] || `pwd`;
chomp($olddir);
unless (-d $olddir) {
  die "Usage: $0 [directory-of-old-hpo-tab-files]";
}

my $newdir = $olddir . ".new";
unless (-d $newdir) { # Warn if it already exists?
  mkdir($newdir) || die("Couldn't create directory $newdir");
}

# Array of new column names by number, e.g. $newColumns[19] = "Assigned by"
my @newColumns = getDesiredColumnArray();
print "getDesiredColumnArray: got $#newColumns cols, last is $newColumns[$#newColumns]\n"; # DEL
# Hash of new column numbers by name, e.g., $columnLookup{"Assigned by"} = 19
my %columnLookup = getColumnNumberHash();
print "Desired col # for Assigned by is " . $columnLookup{"assigned by"} . "\n"; # DEL

my @oldColumns;

# There are too many files in phenotype-commons to do them all at once, so split into groups
# (MIM-1xxxxx.tab, MIM-2xxxxx.tab, etc.)
for (my $i = 1; $i <= 6; $i++) {
  processFiles($i);
}

sub processFiles {
  my ($n) = @_;

  foreach my $oldfile (`/bin/ls $olddir/MIM-$n*.tab`) {
    chomp($oldfile);
    @oldColumns = getColumnNames($oldfile);
    my $newfile = $oldfile;
    $newfile =~ s/$olddir/$newdir/;
    #  print "oldfile = $oldfile, newfile = $newfile\n"; # DEL
    fix($oldfile, $newfile);
  }
}

# Reformat oldfile, save as newfile.
sub fix {
  my ($oldfile, $newfile) = @_;

  if (!(open IN, "<$oldfile")) {
    print "ERROR: can't open $oldfile for reading\n";
    return;
  }
  if (!(open OUT, ">$newfile")) {
    print "ERROR: can't open $newfile for writing\n";
    return;
  }

  # Write the new header line
  foreach my $col (@newColumns) {
    print OUT "$col";
    if ($col eq $newColumns[$#newColumns]) {
      print OUT "\n";
    } else {
      print OUT "\t";
    }
  }

  # Read the data from the old tab file, and save in the new tab file in new format.

  <IN>;   # Skip the first line of input (old column headers) because we already got that

  foreach my $line (<IN>) {
    chomp($line);
    my @cols = fixColValues($line);
#    print "Line from $oldfile ($#cols fields):\n";
#    print "Col 0: $cols[0], Col $#cols: $cols[$#cols]\n";
    for (my $i = 0; $i <= $#cols; $i++) {
      print OUT $cols[$i];
      if ($i == $#cols) {
        print OUT "\n";
      } else {
        print OUT "\t";
      }
    }
 }
  close(IN);
  close(OUT);
  print "Converted old-format file $oldfile to new-format file $newfile\n";
}

# Assign column values to the appropriate new column.
# Check and fix (if necessary) some column values--e.g., if Evidence field says "sdoelken",
# change it to ICE and put sdoelken in "Assigned by".
sub fixColValues {
  my ($line) = @_;
  my @newCols;

  # Split seems to treat consecutive tabs as a single tab, so add a space between tabs for splitting.
  $line =~ s/		/	 	/g;
  my @cols = split('\t', $line);
#  print "fixColValues: num of oldcols = $#oldColumns, num of current cols = $#cols, line = $line\n"; # DEL

  my $evidenceID = $columnLookup{"evidence id"};
  my $assignedBy = "";
  for (my $n = 0; $n <= $#oldColumns; $n++) {
    my $val = $cols[$n];
    my $colName = $oldColumns[$n];
    my $newColNum = $columnLookup{lc $colName};
    # Fix Evidence and Assigned by fields, if required, and save this column value in the appropriate column
    if ($newColNum >= 0 && $newColNum ne "") {
      if ($colName =~ /frequency/i && $val ne "" && $val ne " ") {
        print "Frequency = $val\n"; # DEL
      }
        
      # If Evidence field says "sdoelken", change it to ICE and put sdoelken in "Assigned by".
      if ($colName =~ /evidence name/i) {
        if ($val =~ /doelken/) {
#          print "Evidence col says $val for $line\n";
          $val = "ICE";
          $assignedBy = "sdoelken";
        }
        elsif ($val =~ /IEA/) {
#          print "Evidence col says $val for $line\n";
          $assignedBy = "HPO";
        }
        elsif ($val =~ /PCS/ || $val =~ /TAS/ || $val =~ /ICE/) {
#          print "Evidence col says $val for $line\n";
          $assignedBy = "sdoelken";
        }
        elsif ($val =~ /ITM/) {
#          print "Evidence col says $val for $line\n";
          $assignedBy = "skoehler";
        }

        if ($newCols[$evidenceID] eq "") {
          $newCols[$evidenceID] = $val;
        }
      }

#      print "New col # for $colName is $newColNum (old was $n); val = $val\n"; # DEL
      $newCols[$newColNum] = $val;
    }
    else {
#      print "Ignoring value from unwanted column $colName: $val\n"; # DEL
    }
  }

  # Put appropriate string in "Assigned by" column (unless it's already filled)
  if ($assignedBy ne "") {
    my $assignedByCol = $columnLookup{"assigned by"};
    $newCols[$assignedByCol] = $assignedBy;
#    print "newcols[$assignedByCol] set to $assignedBy\n";
  }
  return @newCols;
}

# Eventually, get column names from human-phenotype.cfg.  For now, just hardcode.
# Note: capitalization of column names doesn't matter because we convert everything to lowercase.
sub getDesiredColumnArray {
  my @col = ("Disease ID", "Disease Name", "Gene ID", "Gene Name", "Genotype", "Gene Symbol(s)", "Phenotype ID", "Phenotype Name", "Age of Onset ID", "Age of Onset Name", "Evidence ID", "Evidence Name", "Frequency", "Sex ID", "Sex Name", "Negation ID", "Negation Name", "Description", "Pub", "Assigned by", "Date Created");
  return @col;
}

# Make a hash of column name (e.g., Assigned by) to column number (19)
sub getColumnNumberHash {
  my %table;
  my @col = getDesiredColumnArray();
  for (my $i = 0; $i <= $#col; $i++) {
#    print "column number for $col[$i] = $i\n"; # DEL
    $table{lc $col[$i]} = $i;
  }
  return %table;
}

# Read column names from first line of inputFile
sub getColumnNames {
  my($inputFile) = @_;

  my $colLine = `head -1 $inputFile`;
  chomp($colLine);
  $colLine =~ s///;
  my @column = split('	', $colLine);
#  print "$inputFile: colline = $colLine\n";
  print "\n$inputFile ($#column fields)\n";
  for (my $i = 0; $i <= $#column; $i++) {
#    print "Old col $i: " . $column[$i] . "\n";
    if ($column[$i] eq "Evidence") {
      $column[$i] = "Evidence name";
#      print "Old col $i was evidence--changed to $column[$i]\n";
    }
  }

  return @column;
}

1;
