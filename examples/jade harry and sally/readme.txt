#author: Emili Boronat
To run this example go up a level to the release folder and open two terminals 
there.

Run this commands one on each termianl.
> java -jar 2apl.jar -nogui -ho master ../../2apl/examples/harry\ and\ sally\ \(còpia\)/harry.mas

> java -jar 2apl.jar -nogui -ho localhost ../../2apl/examples/harry\ and\ sally\ \(còpia\)/sally.mas

Once harry blockwold shows load the wolrd that is in this folders called
'jade_nogui_test_map'

Once harry has this on his map, load the same map into sally.
You should see harry go and pick up the bomb. 

To be fully functional blockworld need to provide a way to be way to sincronize
both world representation for different container. This is on low priority todo
list for now.
