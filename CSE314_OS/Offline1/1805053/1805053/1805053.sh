
stage_dir=$(pwd) 
acceptedOutputPath="$stage_dir/AcceptedOutput.txt"
submissionDir="$stage_dir/Submissions"


max_score=${1:-100}
if (($max_score < 0)); then
    echo "given max score argument negative. Using 100 instead"
    max_score=100
fi

num_students=${2:-5}

if (($num_students < 1 || $num_students > 9 )); then
    echo "given number of students out of range [1 .. 9]. Using 5 instead"
    num_students=5
fi


roll_start=1805121
roll_end=$(($roll_start+$num_students-1))

scores=()

is_in_array(){

    #Unoptimized O(n)

    found="no"
    for key in $1; do
        if [[ $key = $2 ]]; then
            found="yes"
        fi
    done

    echo $found
}


process_script (){
    #runs .sh and saves to temp_out.txt. Compares with AcceptedOutput.txt, overwrites temp_out. grep for </> 
    #and returns mismatch count

    #input path_to_accepted_out path_to_file
    #output mismatch_count

    bash $2>temp_out.txt
    diff -w $1 temp_out.txt>temp_out2.txt
    ret=$(grep -c '<\|>' temp_out2.txt)

    rm -f temp_out.txt temp_out2.txt
    echo $ret
    
}

is_in_array(){

    found="no"
    for key in $1; do
        if [[ $key = $2 ]]; then
            found="yes"
        fi
    done

    echo $found
}



cd $submissionDir

folders=("*")
all_files=()

c=0


#Initial Scoring


for ((i=$roll_start;i<=$roll_end;i++))
    do
    scores[$i]=0
    done



for folder in ${folders[@]}; do
    
    bool_is_in_arr=$(is_in_array "${!scores[*]}" $folder )
    if [[ $bool_is_in_arr = "no" ]]; then
        echo "Folder $folder not in valid ID range"
        continue
    fi

    files_in_folder=("$folder"/*)
    expected_name="$folder/$folder.sh"
    file_name=${files_in_folder[0]}
    
    
    if [[ $(echo $file_name) != "$expected_name" ]]; then
        echo "Malformed File Name. Expected: $expected_name, Got: $file_name"
        continue
    fi


    cd $folder  #change staging directory to student folder 
  
    num_mistakes=$(process_script "$acceptedOutputPath" "$folder.sh")

    cd $submissionDir #change back

    if (( $max_score-num_mistakes*5 < 0 )); then
        scores[$folder]=0
    else
        scores[$folder]=$(( $max_score-num_mistakes*5 ))
    fi

    all_files[$c]=${files_in_folder[0]}
    ((c=c+1))
    # echo $folder
done






#SHOW SCORES BEFORE COPY CHECKER
# for i in ${!scores[@]}; do
#   echo "$i, ${scores[$i]}"
# done

for folder1 in ${folders[@]}; do

    bool_is_in_arr=$(is_in_array "${!scores[*]}" $folder1 )
    if [[ $bool_is_in_arr = "no" ]]; then
        # echo "Folder $folder1 not in valid ID range"
        continue
    fi

    files_in_folder1=("$folder1"/*)
    file1=${files_in_folder1[0]}
   

    res=$(diff -ZB $file1 $acceptedOutputPath)

    if [[ $res = '' ]]; then
        continue
    fi



    for folder2 in ${folders[@]}; do
    bool_is_in_arr=$(is_in_array "${!scores[*]}" $folder2 )
    if [[ $bool_is_in_arr = "no" ]]; then
        # echo "Folder $folder2 not in valid ID range"
        continue
    fi


        if [[ "$folder1" = "$folder2" ]]; then
            continue
        fi


        files_in_folder2=("$folder2"/*)
        file2=${files_in_folder2[0]}
        
        
        
        res=$(diff -ZB $file1 $file2)
        if [[ $res = '' ]]; then

            echo "Plagarism between $file1 and $file2"

            scores[$folder1]=$(( -${scores[$folder1]} ))

            #scores[$folder1]=$(( -$max_score ))

            
        fi
    done
done


echo "student_id   | score"
for i in ${!scores[@]}; do
  echo "$i      | ${scores[$i]}"
done


cd $stage_dir

echo "student_id,score">output.csv

for i in ${!scores[@]}; do
  echo "$i,${scores[$i]}">>output.csv
done

