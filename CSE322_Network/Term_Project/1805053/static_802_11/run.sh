# echo "side------------------------" > data.txt
# for side in 250 500 750 1000 1250
# do  
#     echo "side:$side---------------------" >> data.txt

#     for i in {1..10}
#     do
#         ns static802_11.tcl $side 40 20 100 1 > res.txt
#         awk -f parse.awk trace.tr >> data.txt
        
#     done
#     printf "$side done\n"
# done


echo "nnodes------------------------" > data.txt
for nnodes in 20 40 60 80 100
do  
    echo "nodes:$nnodes---------------------" >> data.txt
    for i in {1..10}
    do
        ns static802_11.tcl 500 $nnodes 20 100 1 > res.txt
        awk -f parse.awk trace.tr >> data.txt
    done
    printf "$nnodes done\n"
done

echo "nflows------------------------" >> data.txt

for nflows in 10 20 30 40 50
do
 	echo "flows:$nflows---------------------" >> data.txt
   
	for i in {1..10}
    do
        ns static802_11.tcl 500 40 $nflows 100 1 > res.txt
        awk -f parse.awk trace.tr >> data.txt
    done
    printf "$nflows done\n"
done


echo "n_packets------------------------" >> data.txt

for n_packets in 100 200 300 400 500
do
 	echo "n_packets:$n_packets---------------------" >> data.txt
   
	for i in {1..10}
    do
        ns static802_11.tcl 500 40 20 $n_packets 1 > res.txt
        awk -f parse.awk trace.tr >> data.txt
    done
    printf "$n_packets done\n"
done

echo "TXRange------------------------" >> data.txt

for TXRange in 1 2 3 4 5
do
 	echo "TXRange:$TXRange---------------------" >> data.txt
   
	for i in {1..10}
    do
        ns static802_11.tcl 500 40 20 100 $TXRange > res.txt
        awk -f parse.awk trace.tr >> data.txt
    done
    printf "$TXRange done\n"
done

