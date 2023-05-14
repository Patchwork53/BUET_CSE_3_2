# echo "side------------------------" > data_802_15.txt
# for side in 250 500 750 1000 1250
# do  
#     echo "side:$side---------------------" >> data_802_15.txt

#     for i in {1..10}
#     do
#         ns mobile802_15.tcl $side 40 20 100 1 >  /dev/null
#         awk -f parse.awk trace.tr >> data_802_15.txt
        
#     done
#     printf "$side done\n"
# done


echo "nnodes------------------------" > data_802_15.txt
for nnodes in 20 40 60 80 100
do  
    echo "nodes:$nnodes---------------------" >> data_802_15.txt
    for i in {1..10}
    do
        ns mobile802_15.tcl 500 $nnodes 20 100 1 >  /dev/null
        awk -f parse.awk trace.tr >> data_802_15.txt
    done
    printf "$nnodes done\n"
done

echo "nflows------------------------" >> data_802_15.txt

for nflows in 10 20 30 40 50
do
 	echo "flows:$nflows---------------------" >> data_802_15.txt
   
	for i in {1..10}
    do
        ns mobile802_15.tcl 500 40 $nflows 100 1 >  /dev/null
        awk -f parse.awk trace.tr >> data_802_15.txt
    done
    printf "$nflows done\n"
done


echo "n_packets------------------------" >> data_802_15.txt

for n_packets in 100 200 300 400 500
do
 	echo "n_packets:$n_packets---------------------" >> data_802_15.txt
   
	for i in {1..10}
    do
        ns mobile802_15.tcl 500 40 20 $n_packets 1 >  /dev/null
        awk -f parse.awk trace.tr >> data_802_15.txt
    done
    printf "$n_packets done\n"
done

echo "speed------------------------" >> data_802_15.txt

for speed in 5 10 15 20 25
do
 	echo "speed:$speed---------------------" >> data_802_15.txt
   
	for i in {1..10}
    do
        ns mobile802_15.tcl 500 40 20 100 $speed >  /dev/null
        awk -f parse.awk trace.tr >> data_802_15.txt
    done
    printf "$speed done\n"
done

