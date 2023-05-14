def means_finder(file_name):
    data_table = []
    with open(file_name, "r") as f:

        for line in f.readlines():
            numbers = line.split(" ")
            data_table.append([float(x) for x in numbers])


    #find mean

    sum = [0]*len(data_table[0])
    for row in data_table:
        for i in range(len(row)):
            sum[i] += row[i]

    means = [x/len(data_table) for x in sum]

    return means


means1 = means_finder("V2.txt")
means2 = means_finder("V1.txt")

with open("data_mean.txt", "w") as f:
    for val1,val2 in zip(means1,means2):
        f.write(str(val1) + " "+ str(val2)+"\n")



#percentage difference between means with reference to means2

# for i in range(len(means1)):
#     print("Mean difference between data1 and data2 for column", i+1, "is", (means1[i]-means2[i])/means2[i]*100, "%")

print("difference energy_consumption: ",  (means1[0]-means2[0])/means2[0]*100)
print("difference sent_packets: ",        (means1[1]-means2[1])/means2[1]*100)
print("difference dropped_packets: ",     (means1[2]-means2[2])/means2[2]*100)
print("difference received_packets: ",    (means1[3]-means2[3])/means2[3]*100)
print("difference throughput: ",          (means1[4]-means2[4])/means2[4]*100)
print("difference avg_delay: ",           (means1[5]-means2[5])/means2[5]*100)
print("difference delivery_ratio: ",      (means1[6]-means2[6])/means2[6]*100)
print("difference drop_ratio: ",          (means1[7]-means2[7])/means2[7]*100)

