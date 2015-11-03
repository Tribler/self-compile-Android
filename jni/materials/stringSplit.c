string str= "Nice Input Man"; 
int len = strlen(str);
int OS; //output sting number 
string output[]; 
for (int loop = 0; loop < len; loop++)
{ 
  if ( str[loop] == '\t')
  { OS += 1; }
  else 
  { output[OS] += str[loop]; } 
} 


Here is a simple example showing the use of strtok.

#include <string.h>
#include <stddef.h>

...

char string[] = "words separated by spaces -- and, punctuation!";
const char delimiters[] = " .,;:!-";
char *token;

...

token = strtok (string, delimiters);  /* token => "words" */
token = strtok (NULL, delimiters);    /* token => "separated" */
token = strtok (NULL, delimiters);    /* token => "by" */
token = strtok (NULL, delimiters);    /* token => "spaces" */
token = strtok (NULL, delimiters);    /* token => "and" */
token = strtok (NULL, delimiters);    /* token => "punctuation" */
token = strtok (NULL, delimiters);    /* token => NULL */
