#! /bin/csh -f

setenv COCKER_HOME /vol/cocker
setenv COCKER_INDEX /vol/cocker_index

set A = STMTSEARCHGLOBAL
set P = 10268
set H = cocker.cs.brown.edu
set C = ( /vol/fullCorpus )

$COCKER_HOME/bin/cockercmd -a $A -stop

$COCKER_HOME/bin/cockerdb -a $A new -dir $COCKER_INDEX

$COCKER_HOME/bin/cockercmd -a $A start -M 16000 -dir $COCKER_INDEX

foreach i ( $C )
   $COCKER_HOME/bin/cockercmd -a $A -m $i -dir $COCKER_INDEX
end











