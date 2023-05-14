
echo "=---------------802.15.4--------------------" > data_802_15.txt
for i in {1..1}
do
    ns mobile802_15.tcl 500 40 20 100 15 > /dev/null
    awk -f parse.awk trace.tr >> data_802_15.txt
done
