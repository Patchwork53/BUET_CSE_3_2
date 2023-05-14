echo "-----------802_11-----------"> data_802_11.txt
for i in {1..50}
do
    ns static802_11.tcl 500 40 20 200 3 > /dev/null
    awk -f parse.awk trace.tr >> data_802_11.txt
done
