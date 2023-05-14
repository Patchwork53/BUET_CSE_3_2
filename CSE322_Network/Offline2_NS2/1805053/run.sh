
echo "side------------------------" > data.txt
for side in 250 500 750 1000 1250
do  
    echo "side:$side---------------------" >> data.txt

    for i in {1..10}
    do
        ns wireless.tcl $side 40 20 > res.txt
        awk -f parse.awk trace.tr >> data.txt
        
    done
    printf "$side done\n"
done


echo "nnodes------------------------" >> data.txt
for nnodes in 20 40 60 80 100
do  
    echo "nodes:$nnodes---------------------" >> data.txt
    for i in {1..10}
    do
        ns wireless.tcl 500 $nnodes 20 > res.txt
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
        ns wireless.tcl 500 40 $nflows > res.txt
        awk -f parse.awk trace.tr >> data.txt
    done
    printf "$nflows done\n"
done

