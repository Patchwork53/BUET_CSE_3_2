import pandas as pd
import numpy as np
from collections import defaultdict
import math
import random
random.seed(2022)



class Graph:
    def __init__(self, course_data, student_data) -> None:

        self.n = course_data.shape[0]
        self.course_adj_list = dict()
        self.static_course_adj_list = dict()
        self.not_these_colors = dict()
        self.assignments = dict()
        self.raw_course_data = course_data
        self.raw_student_data = student_data
        self.course_data = sorted(course_data, key=lambda x: x[1], reverse=True)
     

        for i in range(1,self.n+1):
            self.not_these_colors[i] = set()

       
        for i in range(student_data.shape[0]):
            for course1 in student_data[i]:
                for course2 in student_data[i]:
                    if course1 not in self.course_adj_list:
                        self.course_adj_list[course1] = set()
                    if course1 not in self.static_course_adj_list:
                        self.static_course_adj_list[course1] = set()
                    if course1 != course2:
                    
                        self.course_adj_list[course1].add(course2)
                        self.static_course_adj_list[course1].add(course2)

    
    def get_max_adj(self):
        max = -10000
        max_key = -1
        # print("remaining", len(self.course_adj_list))
        for key in self.course_adj_list.keys():
            if len(self.course_adj_list[key]) > max:
                max = len(self.course_adj_list[key])
                max_key = key
        # print(max_key, max, self.course_adj_list[max_key])
        return max_key

    def get_max_enrollment(self):
        ret =  self.course_data[0][0]
        self.course_data = self.course_data[1:]
        return ret

    def get_dsatur(self):
        max_colors = -1
        max_key = -1
        for key in self.not_these_colors.keys():
            if len(self.not_these_colors[key]) > max_colors:
                max_colors = len(self.not_these_colors[key])
                max_key = key
        return max_key

    def get_random(self):
        r =  np.random.randint(0, len(self.course_data))
        ret = self.course_data[r][0]
        self.course_data = np.delete(self.course_data, r, 0)
        return ret

    def most_restricted_node(self):
        max = -100000
        max_key = -1
        for key in self.course_adj_list.keys():
            if len(self.not_these_colors[key]) > max:
                max = len(self.not_these_colors[key])
                max_key = key
        return max_key

    def print_colors(self,kempe_chain):
        for node in kempe_chain:
            print("[",node, self.assignments[node],"]", end=" ")
        print()
    
    def get_average_penalty(self):
        linear_penalty = 0
        exp_penalty = 0
        threshold = 5
        for student_courses in self.raw_student_data:
            for i in range(len(student_courses)):
                for j in range(i+1,len(student_courses)):
                    day1 = self.assignments[student_courses[i]]
                    day2 = self.assignments[student_courses[j]]
                    gap = abs(day1-day2)
                    if gap > threshold:
                        continue
                    linear_penalty += 2*(threshold-gap)
                    exp_penalty += pow(2,(threshold-gap))

        return linear_penalty/self.raw_student_data.shape[0], exp_penalty/self.raw_student_data.shape[0] 
    
    def initial_solution(self, heuristic):
        while len(self.assignments) < self.n:
            
            to_color = heuristic()
         
            if to_color == -1:
                with open("output.txt", "w") as f:
                    for nodes in sorted(self.assignments.keys()):
                        f.write(str(nodes) + " " + str(self.assignments[nodes]) + "\n")
                    
            
            for color in range(self.n+1):
                if color not in self.not_these_colors[to_color]:
                    self.assignments[to_color] = color
                    self._update_not_these_colors(to_color, color)
                    self._update_course_adj_conflict(to_color)
                    break
      
        max_color = -1
        for key in self.assignments.keys():
            if self.assignments[key] > max_color:
                max_color = self.assignments[key]
        return max_color+1
       
    def _update_not_these_colors(self, course, color):
        for neighbours in self.course_adj_list[course]:
            self.not_these_colors[neighbours].add(color)
        self.not_these_colors.pop(course)

    def _update_course_adj_conflict(self, course):
        for neighbours in self.course_adj_list[course]:
            self.course_adj_list[neighbours].remove(course)
        
        self.course_adj_list.pop(course)

        

    def _get_next_in_kempe_chain(self, curr_node, prev_node, kempe_chain):
        required_color = self.assignments[prev_node]
        temp_list = list(self.static_course_adj_list[curr_node])
        random.shuffle(temp_list)
        for adj_course in temp_list:
            if len(kempe_chain)==2 and adj_course == kempe_chain[0]:
                continue
            if self.assignments[adj_course] == required_color:
                if adj_course in kempe_chain and kempe_chain[0]!=adj_course:
                    continue
                return adj_course

        return None

    def _get_random_kempe_chain(self):
        '''returns a random kempe chain (a circular graph)'''
        keys_list = list(self.static_course_adj_list.keys())
        random.shuffle(keys_list)

        for course1 in keys_list:
            for course2 in self.static_course_adj_list[course1]:
                kempe_chain = []
                kempe_chain.append(course1)
                kempe_chain.append(course2)
                
                prev_node = course1
                curr_node = course2
                while True:
                    temp = curr_node
                    curr_node = self._get_next_in_kempe_chain(curr_node, prev_node, kempe_chain)
                    if curr_node == course1:
                        return kempe_chain
                    if curr_node == None: #fail to find kempe chain. no next node can take the color of prev node
                        break
                    kempe_chain.append(curr_node)
                    prev_node = temp

        return None

    def _get_kempe_tree(self, root_course, next_color, kempe_tree_set):
        '''returns a 2 color connected subgraph including root'''
        kempe_tree_set.add(root_course)

        prev_color = self.assignments[root_course]
        
        for course2 in self.static_course_adj_list[root_course]:
            if course2 in kempe_tree_set:
                continue

            if self.assignments[course2] == next_color:
                self._get_kempe_tree(course2, prev_color, kempe_tree_set)

    def _get_random_kempe_tree(self):
        keys_list = list(self.static_course_adj_list.keys())
        random.shuffle(keys_list)

        while True:
            for course1 in keys_list:
                adj_colors = set([self.assignments[course2] for course2 in self.static_course_adj_list[course1]])
                for second_color in adj_colors:
                
                    kempe_tree_set = set()
                    self._get_kempe_tree(course1,second_color, kempe_tree_set)

                    if len(kempe_tree_set) > 2:
                        return kempe_tree_set

    def _switch_colors(self, kempe_chain):
        colors = list(set([self.assignments[course] for course in kempe_chain]))
        first_color = colors[0]
        second_color = colors[1]

        for node in kempe_chain:
            if self.assignments[node]==first_color:
                self.assignments[node]=second_color
            else:
                self.assignments[node]=first_color


    def _get_random_valid_pair(self):

        while True:
            
            course1 = random.randint(1,self.n)
            course2 = random.randint(1,self.n)
            if course1 == course2:
                continue

            color1 = self.assignments[course1]
            color2 = self.assignments[course2]

            if color1 == color2:
                continue

            adj_colors1 = set()
            adj_colors2 = set()

            for adj in self.static_course_adj_list[course1]:
                if adj == course2:
                    continue
                adj_colors1.add(self.assignments[adj])
            
            for adj in self.static_course_adj_list[course2]:
                if adj == course1:
                    continue
                adj_colors2.add(self.assignments[adj])
            
         
            if color1 in adj_colors2 or color2 in adj_colors1:
                continue
                
            return [course1, course2]


    def refine_solution_pair_swap(self, verbose = 1, epochs = 1000, penalty_idx = 1):
        '''penalty_idx = 0 for linear, 1 for exponential'''
        for epoch in range(epochs):
            penalty_before = self.get_average_penalty()[penalty_idx]
            pair = self._get_random_valid_pair()
            
           
            self._switch_colors(pair)

            if self.check_some_nodes(pair)==False:
                print("error")

            penalty_after = self.get_average_penalty()[penalty_idx]

            if penalty_after > penalty_before or self.check_some_nodes(pair)==False:
               
                self._switch_colors(pair)
              
                if  self.get_average_penalty()[penalty_idx]!=penalty_before:
                    print("Before:", penalty_before, "After:", self.get_average_penalty()[0])
                    print('error')
                    break
            
               
            if verbose==1 and epoch%100==0:
                print('epoch: {}, penalty: {}'.format(epoch, penalty_before))
            elif verbose==2:
                print('epoch: {}, penalty: {}'.format(epoch, penalty_before))
           
    def refine_solution_kempe_chain(self,verbose = 1, epochs=1000, penalty_idx = 1):
        '''penalty_idx = 0 for linear, 1 for exponential'''
        for epoch in range(epochs):
          
            penalty_before = self.get_average_penalty()[penalty_idx]
            kempe_chain = self._get_random_kempe_tree()
            
            self._switch_colors(kempe_chain)
            penalty_after = self.get_average_penalty()[penalty_idx]

            if penalty_after > penalty_before or self.check_some_nodes(kempe_chain) == False:
               
                self._switch_colors(kempe_chain)
        
                if  self.get_average_penalty()[penalty_idx]!=penalty_before:
                    print("Before:", penalty_before, "After: ", self.get_average_penalty()[0])
                    print('error')
                    break
            
            
            if verbose==1 and epoch%100==0:
                print('epoch: {}, penalty: {}'.format(epoch, penalty_before))
            elif verbose==2:
                print('epoch: {}, penalty: {}'.format(epoch, penalty_before))

    def save_solution(self, filename):
        ordered_keys = sorted(self.assignments.keys())
        with open(filename, 'w') as f:
            for key in ordered_keys:
                f.write(str(key) + ' ' + str(self.assignments[key])+'\n')
    
    def read_solution(self, filename):
        with open(filename, 'r') as f:
            for line in f:
                line = line.split()
                self.assignments[int(line[0])] = int(line[1])-1
        
    def check_solution(self):
        for key in self.assignments.keys():
            for adj in self.static_course_adj_list[key]:
                if self.assignments[key] == self.assignments[adj]:
                    print("conflict",key, adj)
                    print("colors", self.assignments[key], self.assignments[adj])
                    return False
        return True

    def check_some_nodes(self, nodes):
        for node in nodes:
            for adj in self.static_course_adj_list[node]:
                if self.assignments[node] == self.assignments[adj]:
                    return False
        return True


if __name__ == "__main__":
            
    problems = [ "Toronto/car-s-91","Toronto/car-f-92", "Toronto/kfu-s-93", "Toronto/tre-s-92","Toronto/yor-f-83"]

    with open('benchmark_ignore.txt',mode='w' ) as benchmark_file:
        for problem_idx in range(len(problems)):
            
            #read a space seperated file
            course_data = pd.read_csv(problems[problem_idx]+'.crs', sep=' ',header=None).to_numpy()

            student_data = []

            with open(problems[problem_idx]+'.stu') as f:
                for line in f:
                    stu_crs = [int(i) for i in line.split(" ")[:-1]]
                    student_data.append(np.array(stu_crs))
                    

            student_data = np.array(student_data[:-1],dtype=object)
            print("number of courses:", course_data.shape[0])
            print("number of students:",student_data.shape[0])

            benchmark_file.write(problems[problem_idx]+' courses:'+str(course_data.shape[0])+' students:'+str(student_data.shape[0])+'\n')

            graph = Graph(course_data, student_data)

            ##########################################################
            #########################################################
            ########################################################
            #######################################################
            colors_used = graph.initial_solution(graph.get_max_adj)
            #######################################################
            ########################################################
            #########################################################
            ##########################################################

            benchmark_file.write('colors used: '+str(colors_used)+'\n')
            print("colors used:", colors_used)

            benchmark_file.write('initial solution: '+str(graph.get_average_penalty())+'\n')
            print(graph.check_solution())


            graph.refine_solution_kempe_chain(epochs=3000, penalty_idx=0)
            benchmark_file.write('after kempe: '+str(graph.get_average_penalty())+'\n')

            graph.refine_solution_pair_swap(epochs=3000, penalty_idx=0)
            benchmark_file.write('after pair_swap: '+str(graph.get_average_penalty())+'\n')
        
            print(graph.check_solution())

            graph.save_solution(problems[problem_idx]+'.sol')
            benchmark_file.flush()



