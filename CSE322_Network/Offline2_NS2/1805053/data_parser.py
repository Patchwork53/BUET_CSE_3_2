import statistics
iterations = 10
array_of_data = []

count = 0
data = []
with open('data.txt') as f:
    for line in f:
        line = line[:-1]
        if len(line.split(" ")) == 1:
            
            if count != 0:
                array_of_medians = []
                for col_idx in range(len(array_of_data[0])):
                    col = [row[col_idx] for row in array_of_data]
                    array_of_medians.append(statistics.median(col))
                data.append(array_of_medians)
                print(array_of_medians)

            array_of_data = []
            print(line)
            count = 0
          
        else:
            if "nan" in line:
                continue
            array_of_data.append([float(val) for val in line.split(" ")])

            count += 1
     
    array_of_medians = []
    for col_idx in range(len(array_of_data[0])):
        col = [row[col_idx] for row in array_of_data]
        array_of_medians.append(statistics.median(col))
    data.append(array_of_medians)
    print(array_of_medians)

#convert to csv
with open('data_median.csv', 'w') as f:
    for row in data:
        f.write(",".join([str(val) for val in row]) + "\n")

    