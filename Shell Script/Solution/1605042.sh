#!/bin/bash
input=""
inputs=""
portion=""
portionCount=""
word=""
array=""
temp=""
srcResultFiles=""
destResultFiles=""
root=""
n=""
if [ $# -eq 1 ] 
then
input=$1
i=0;
	if (test -f  "$input"); then
	while read -r line; do
	inputs[$i]=$line
	i=`expr $i + 1`
	done < $input 
	portion=${inputs[0]}
	portionCount=${inputs[1]}
	word=${inputs[2]}
	OLDIFS=$IFS
	IFS="/"
	read -a array <<< "$(printf "%s" "$PWD")"
	IFS=$OLDIFS
	n="${#array[@]}"
	n=`expr $n - 1`
	root=${array[n]}
	else
	echo "Please give a valid input file name"
	exit
	fi
elif [ $# -eq 2 ]
then
input=$2
i=0;
	if (test -f  "$input"); then
	while read -r line; do
	inputs[$i]=$line
	i=`expr $i + 1`
	done < $input 
	portion=${inputs[0]}
	portionCount=${inputs[1]}
	word=${inputs[2]}
	cd "$1"
	OLDIFS=$IFS
	IFS="/"
	read -a array <<< "$(printf "%s" "$1")"
	IFS=$OLDIFS
	n="${#array[@]}"
	n=`expr $n - 1`
	root=${array[n]}
	array="";
	else
	echo "Please give a valid input file name"
	exit
	fi
else
echo "You may provide the working directory(optional) and input file name"
exit
fi
IFS=$'\r\n' GLOBIGNORE='*' command eval  'temp=($(grep -r -i -l "$word"))'
#for i in "${temp[@]}"
#do
#echo "$i"
#done
#grep -n -i "hello" abc.txt|head -1| cut -d':' -f1
j=0
if [ $portion = begin ]
then
	for i in "${temp[@]}"
	do
	line=($(grep -n -i "$word" "$i"|head -1| cut -d':' -f1))
		if [ $line -le $portionCount ]
		then
		srcResultFiles[$j]=$i;
		OLDIFS=$IFS
		IFS="."
		read -a array <<< "$(printf "%s" "$i")"
		IFS=$OLDIFS
		n="${#array[@]}"
		if [ $n -ge 2 ]
		then
			n=`expr $n - 2`
			array[$n]="${array[$n]}$line."
		else
			array[0]="${array[0]}$line"
		fi
		destResultFiles[$j]="";
		for k in "${array[@]}"
		do
			destResultFiles[$j]="${destResultFiles[$j]}$k"
		done
		destResultFiles[$j]=$(echo "$root.${destResultFiles[$j]}" | sed "s#/#.#g")
		#echo ${destResultFiles[$j]}
		j=`expr $j + 1`
		fi
	done
	#for m in "${srcResultFiles[@]}"
	#do
	#echo "$m"
	#done
elif [ $portion = end ]
then
	for i in "${temp[@]}"
	do
	minLine=$(wc -l < "$i")
	minLine=`expr $minLine - $portionCount`
	line=($(grep -n -i "$word" "$i"|tail -1| cut -d':' -f1))
		if [ $line -ge $minLine ]
		then
		srcResultFiles[$j]=$i;
		OLDIFS=$IFS
		IFS="."
		read -a array <<< "$(printf "%s" "$i")"
		IFS=$OLDIFS
		n="${#array[@]}"
		if [ $n -ge 2 ]
		then
			n=`expr $n - 2`
			array[$n]="${array[$n]}$line."
		else
			array[0]="${array[0]}$line"
		fi
		destResultFiles[$j]="";
		for k in "${array[@]}"
		do
			destResultFiles[$j]="${destResultFiles[$j]}$k"
		done
		destResultFiles[$j]=$(echo "$root.${destResultFiles[$j]}" | sed "s#/#.#g")
		#echo ${destResultFiles[$j]}
		j=`expr $j + 1`
		fi
	done
	#for m in "${srcResultFiles[@]}"
	#do
	#echo "$m"
	#done
fi
n="${#srcResultFiles[@]}"
echo "Total number of files​ which matched the criteria : $n"
if [ -d "../output_dir" ]; then rm -Rf ../output_dir; fi
mkdir  ../output_dir
j=0;
for i in "${srcResultFiles[@]}"
do
cp "$i" "../output_dir/""${destResultFiles[$j]}"""
j=`expr $j + 1`
done
rm -f ../output.csv
touch  ../output.csv
echo File Path,Line Number,Line Containing Searched String >> ../output.csv

if [ $portion = begin ]
then
for i in "${srcResultFiles[@]}"
do
#echo $i
#n=$(grep -n -i -c "$word" "$i")
IFS=$'\r\n' GLOBIGNORE='*' command eval  'singleResult=($(grep -n -i  "$word" "$i"))'
#singleResult="$(grep -n -i  "$word" "$i")"
for j in "${singleResult[@]}"
do
testLineNo=$(echo $j| cut -d':' -f1)
sentence=$(echo $j| cut -d':' -f2-)
if [ $testLineNo -le $portionCount ]
then
echo "\"$root/$i\",\"$testLineNo\",\"$sentence\"" >> ../output.csv
fi
done
#echo $root"/"$i,$(grep -n -i  "$word" "$i"| cut -d':' -f1-),$(grep -n -i  "$word" "$i"| cut -d':' -f2-) >> ../output.csv
#echo "${singleResult[1]}"
#echo "$n"
done
fi

if [ $portion = end ]
then
for i in "${srcResultFiles[@]}"
do
minLine=$(wc -l < "$i")
minLine=`expr $minLine - $portionCount`
#echo $i
#n=$(grep -n -i -c "$word" "$i")
IFS=$'\r\n' GLOBIGNORE='*' command eval  'singleResult=($(grep -n -i  "$word" "$i"))'
#singleResult="$(grep -n -i  "$word" "$i")"
for j in "${singleResult[@]}"
do
testLineNo=$(echo $j| cut -d':' -f1)
sentence=$(echo $j| cut -d':' -f2-)
if [ $testLineNo -ge $minLine ]
then
echo "\"$root/$i\",\"$testLineNo\",\"$sentence\"" >> ../output.csv
fi
done
#echo $root"/"$i,$(grep -n -i  "$word" "$i"| cut -d':' -f1-),$(grep -n -i  "$word" "$i"| cut -d':' -f2-) >> ../output.csv
#echo "${singleResult[1]}"
#echo "$n"
done
fi



